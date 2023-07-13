package tokenico.models;

import java.math.BigInteger;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Accessors(fluent = true)
@Builder
public class Sale {
    final boolean created;
    final BigInteger startTime;
    final BigInteger endTime;
    final BigInteger price;
    final boolean openStatus;
    final BigInteger totalSold;
}
