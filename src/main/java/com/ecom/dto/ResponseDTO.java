package com.ecom.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include. NON_NULL)
public class ResponseDTO<T> {
    private String status;
    private String responseCode;
    private String responseDescription;
    private T data;
    @Contract("_, _ -> new")
    public static  <V> @NotNull ResponseDTO ok(V data, String description) {
        return new ResponseDTO("ok", "200", description, data);
    }
    public static  <V> @NotNull ResponseDTO success(V data, String description) {
        return new ResponseDTO("SUCCESS", "201", description, data);
    }
    public static <V> @NotNull ResponseDTO notFound(V data, String description) {
        return new ResponseDTO("NOT_FOUND", "404", description,data);
    }

    public static <V> @NotNull ResponseDTO internalServerError(V data, String description) {
        return new ResponseDTO("INTERNAL_SERVER_ERROR", "500", description,data);
    }
    public static<V> @NotNull ResponseDTO Bad_Request(V data, String description) {
        return new ResponseDTO("BAD_REQUEST", "400", description,data);
    }
    public static<V> @NotNull ResponseDTO forbidden(V data, String description) {
        return new ResponseDTO("FORBIDDEN", "402", description,data);
    }
    public static<V> @NotNull ResponseDTO un_Authorized(V data, String description) {
        return new ResponseDTO("UNAUTHORIZED", "401", description,data);
    }


}
