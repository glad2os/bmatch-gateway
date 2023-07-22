package quest.dine.gateway.dto;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Data
public class ApiResponse {
    private final String message;
    private final int statusCode;
}