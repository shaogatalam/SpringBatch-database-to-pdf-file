package BasePack.Model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class CustomerOrderSummary {
    private Long customerId;
    private Double totalSpend;
    private Integer totalOrders;
    private LocalDate lastOrderDate;

    public CustomerOrderSummary(Long customerId, Double totalSpend, Integer totalOrders, LocalDate lastOrderDate) {
        this.customerId = customerId;
        this.totalSpend = totalSpend;
        this.totalOrders = totalOrders;
        this.lastOrderDate = lastOrderDate;
    }
}
