import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

enum DishSize { SMALL, MEDIUM, LARGE }

enum OrderStatus {
    PENDING, CONFIRMED, PREPARING, IN_DELIVERY, DELIVERED, CANCELLED;

    // S3 : les transitions autorisées du cycle de vie
    boolean canMoveTo(OrderStatus next) {
        return switch (this) {
            case PENDING -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED -> next == PREPARING || next == CANCELLED;
            case PREPARING -> next == IN_DELIVERY || next == CANCELLED;
            case IN_DELIVERY -> next == DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };
    }
}

/** Valeur immuable : un plat du menu. */
record Dish(String name, BigDecimal price, DishSize size) {
    Dish {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(price, "price");
        Objects.requireNonNull(size, "size");
        if (name.isBlank()) throw new IllegalArgumentException("Nom vide");
        if (price.signum() < 0) throw new IllegalArgumentException("Prix négatif : " + price);
        price = price.setScale(2, RoundingMode.HALF_UP); // 12.5 et 12.50 deviennent égaux
    }
}

/** Héritage : ce que partagent tous les utilisateurs de la plateforme. */
abstract class User {
    private final UUID id = UUID.randomUUID();
    private final String name;
    private final String email;

    protected User(String name, String email) {
        this.name = Objects.requireNonNull(name);
        this.email = Objects.requireNonNull(email);
    }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public abstract String role();
}

class Customer extends User {
    private final String address;
    Customer(String name, String email, String address) {
        super(name, email);
        this.address = Objects.requireNonNull(address);
    }
    public String getAddress() { return address; }
    @Override public String role() { return "CLIENT"; }
}

class Courier extends User {
    Courier(String name, String email) { super(name, email); }
    @Override public String role() { return "LIVREUR"; }
}

/** Entité mutable, encapsulée : une commande. */
class Order {
    private final String id = "order-" + UUID.randomUUID();
    private final Customer customer;
    private final Map<Dish, Integer> items = new LinkedHashMap<>();
    private final LocalDateTime createdAt = LocalDateTime.now();
    private OrderStatus status = OrderStatus.PENDING;

    Order(Customer customer) { this.customer = Objects.requireNonNull(customer); }

    public void addDish(Dish dish, int quantity) {
        if (status != OrderStatus.PENDING) throw new IllegalStateException("Commande déjà validée");
        if (quantity <= 0) throw new IllegalArgumentException("Quantité invalide : " + quantity);
        items.merge(dish, quantity, Integer::sum);
    }

    public BigDecimal getTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Dish, Integer> e : items.entrySet()) {
            total = total.add(e.getKey().price().multiply(BigDecimal.valueOf(e.getValue())));
        }
        return total;
    }

    public void confirm() {
        if (status != OrderStatus.PENDING || items.isEmpty()) throw new IllegalStateException("Confirmation impossible");
        status = OrderStatus.CONFIRMED;
    }

    // S3 : changement de statut contrôlé
    public void moveTo(OrderStatus next) {
        Objects.requireNonNull(next, "next");
        if (!status.canMoveTo(next)) {
            throw new IllegalStateException("Transition interdite : " + status + " -> " + next);
        }
        if (next == OrderStatus.CONFIRMED && items.isEmpty()) {
            throw new IllegalStateException("Commande vide");
        }
        status = next;
    }

    public String getId() { return id; }
    public Customer getCustomer() { return customer; }
    public Map<Dish, Integer> getItems() { return Collections.unmodifiableMap(items); }
    public OrderStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

/** Interface + polymorphisme. */
interface Discount {
    BigDecimal apply(BigDecimal total);
}

record PercentageDiscount(int percent) implements Discount {
    PercentageDiscount {
        if (percent < 0 || percent > 100) throw new IllegalArgumentException("Pourcentage invalide : " + percent);
    }
    public BigDecimal apply(BigDecimal total) {
        return total.multiply(BigDecimal.valueOf(100 - percent))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}

record FixedDiscount(BigDecimal amount) implements Discount {
    public BigDecimal apply(BigDecimal total) { return total.subtract(amount).max(BigDecimal.ZERO); }
}

class DeliveryPlatform {
    private final Map<String, Order> orders = new HashMap<>();
    public void placeOrder(Order order) { orders.put(order.getId(), order); }
    public Optional<Order> findOrderById(String id) { return Optional.ofNullable(orders.get(id)); }
    public Collection<Order> getOrders() { return Collections.unmodifiableCollection(orders.values()); }

    // S3 : exception métier et recherches avec les streams
    public Order getOrderById(String id) {
        return findOrderById(id).orElseThrow(() -> new OrderNotFoundException(id));
    }

    public List<Order> findOrdersByStatus(OrderStatus status) {
        return orders.values().stream()
                     .filter(o -> o.getStatus() == status)
                     .toList();
    }

    public List<Order> findOrdersByCustomer(Customer customer) {
        return orders.values().stream()
                     .filter(o -> o.getCustomer().getId().equals(customer.getId()))
                     .toList();
    }
}

/** S3 : exception métier (unchecked). */
class OrderNotFoundException extends RuntimeException {
    private final String orderId;

    OrderNotFoundException(String orderId) {
        super("Commande introuvable : " + orderId);
        this.orderId = orderId;
    }

    public String getOrderId() { return orderId; }
}

public class FoodFastModel {
    public static void main(String[] args) {
        Dish pizza = new Dish("Margherita", new BigDecimal("12.5"), DishSize.MEDIUM);
        Dish same = new Dish("Margherita", new BigDecimal("12.50"), DishSize.MEDIUM);
        Customer alice = new Customer("Alice", "alice@example.com", "10 rue de Paris");
        Order order = new Order(alice);
        order.addDish(pizza, 2);
        order.addDish(same, 1);
        order.confirm();
        Discount d = new PercentageDiscount(10);
        DeliveryPlatform platform = new DeliveryPlatform();
        platform.placeOrder(order);
        IO.println(order.getItems() + " total=" + order.getTotal() + " remise=" + d.apply(order.getTotal()));
        IO.println(platform.findOrderById(order.getId()).map(Order::getStatus).orElseThrow());
        IO.println(alice.role() + " " + pizza.equals(same));
        order.moveTo(OrderStatus.PREPARING);
        IO.println(platform.findOrdersByStatus(OrderStatus.PREPARING).size() + " commande(s) en préparation");
    }
}
