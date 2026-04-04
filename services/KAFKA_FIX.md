# Kafka Type Mapping Fix

## Issue

The risk-engine service was failing with the following error:

```
java.lang.IllegalArgumentException: The class 'worker.registered' is not in the trusted packages: 
[java.util, java.lang, com.parametrix.common.events]
```

## Root Cause

**Kafka Type ID Mismatch:**
- The producer (api-gateway) was sending events with a **simple type ID** like `worker.registered`
- The consumer (risk-engine and other services) expected the **full class name** like `com.parametrix.common.events.WorkerRegisteredEvent`
- The type mapping configuration was missing in consumer services

## Solution

Added **Kafka type mappings** to all producer and consumer configurations in `application.yml` files.

### Type Mapping Configuration

All services now include this mapping in their Kafka producer and consumer properties:

```yaml
spring.json.type.mapping: >
  worker.registered:com.parametrix.common.events.WorkerRegisteredEvent,
  policy.purchased:com.parametrix.common.events.PolicyPurchasedEvent,
  environment.disruption:com.parametrix.common.events.EnvironmentDisruptionEvent,
  claim.initiated:com.parametrix.common.events.ClaimInitiatedEvent,
  claim.validated:com.parametrix.common.events.ClaimValidatedEvent,
  claim.approved:com.parametrix.common.events.ClaimApprovedEvent,
  payout.completed:com.parametrix.common.events.PayoutCompletedEvent
```

This tells Spring Kafka to:
1. **When producing**: Send type ID as `worker.registered` instead of full class name
2. **When consuming**: Map `worker.registered` back to `com.parametrix.common.events.WorkerRegisteredEvent`

## Files Modified

Updated `application.yml` in all services:
- ✅ `services/api-gateway/src/main/resources/application.yml`
- ✅ `services/risk-engine/src/main/resources/application.yml`
- ✅ `services/trigger-engine/src/main/resources/application.yml`
- ✅ `services/fraud-detection/src/main/resources/application.yml`
- ✅ `services/claim-service/src/main/resources/application.yml`
- ✅ `services/payout-service/src/main/resources/application.yml`

## Testing

1. **Rebuild services:**
   ```bash
   cd services
   ./build.sh --skip-tests
   ```

2. **Rebuild Docker images:**
   ```bash
   docker-compose build
   ```

3. **Restart services:**
   ```bash
   docker-compose down
   docker-compose up -d
   ```

4. **Verify logs:**
   ```bash
   docker-compose logs -f risk-engine
   ```

   Should no longer show serialization errors.

## Why This Happened

The event classes define a `TYPE` constant for simplified event identification:

```java
public class WorkerRegisteredEvent extends BaseEvent {
    public static final String TYPE = "worker.registered";
    // ...
}
```

The producer was configured to use this type ID, but consumers didn't have the reverse mapping to understand what class `worker.registered` refers to.

## Prevention

**Best Practice:** Always configure type mappings consistently across all services that produce or consume Kafka messages. Include ALL event types in the mapping, even if a service doesn't use all of them (for consistency and future-proofing).
