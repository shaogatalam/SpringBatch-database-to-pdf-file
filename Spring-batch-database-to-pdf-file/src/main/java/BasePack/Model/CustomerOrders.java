package BasePack.Model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerOrders {
    private Long customerId;
    private Double totalSpend;
    private Integer totalOrders;
    private LocalDate lastOrderDate;
}