# NOTES.md

## Commands used

### Reproduce customer summary issue

curl http://localhost:8080/api/payments/customer/1/summary

curl http://localhost:8080/api/payments/customer/2/summary

### Create payment (Git Bash)

curl -X POST http://localhost:8080/api/payments/process
-H "Content-Type: application/json"
-d '{
"customerId":1,
"amount":100,
"creditCardNumber":"4111111111111111"
}'

### Test refund endpoint

curl -X POST http://localhost:8080/api/payments/refund
-H "Content-Type: application/json"
-d '{
"transactionId":1
}'

---

## Finding #1 - Customer Summary Endpoint

While testing the endpoint `/api/payments/customer/{id}/summary`, requests for some customers returned HTTP 500. The application logs showed a `LazyInitializationException` when accessing `customer.getTransactions().size()`.

The issue occurs because the `transactions` collection is lazily loaded and is accessed in the controller after the transaction/session has already been closed. This only affects VIP customers because the collection is accessed only for that segment, making the error appear random.

### Fix Applied

Added a repository method using `JOIN FETCH` to load the customer's transactions together with the customer entity before the Hibernate session is closed. After the change, the endpoint returns successful responses consistently.

### Alternative Considered

Moving the summary calculation into the service layer so that the collection would be accessed inside the transactional context. I chose the `JOIN FETCH` approach because it requires fewer changes and directly addresses the root cause.

---

## Finding #2 - Sensitive Information in Logs

The payment processing flow logged full credit card numbers.

### Fix Applied

Masked the credit card number before logging it, exposing only the last four digits. This prevents sensitive payment information from being written to application logs while preserving traceability for troubleshooting.

---

## Refund Endpoint

Implemented `POST /api/payments/refund`.

Validation rules:

* Transaction must exist.
* Transaction status must be `APPROVED`.
* Transaction must not already have an associated refund.

If all validations pass, a new transaction is created:

* Same customer as the original transaction.
* Negative amount of the original transaction.
* Status `REFUNDED`.
* Associated to the original transaction to prevent duplicate refunds.


---

## Additional Improvements

### Custom Exceptions

Replaced generic `RuntimeException` usages with domain-specific exceptions:

* `CustomerNotFoundException`
* `TransactionNotFoundException`
* `RefundAlreadyExistsException`

Benefits:

* Clearer business intent.
* Easier troubleshooting and log analysis.
* Better maintainability.
* Enables mapping different business errors to appropriate HTTP status codes.

---

### Transaction Status Enum

Introduced a dedicated `TransactionStatus` enum and replaced String-based transaction statuses.

Previous implementation:

* APPROVED
* REJECTED
* PENDING
* REFUNDED

were represented as String values.

New implementation:

```java
TransactionStatus.APPROVED
TransactionStatus.REJECTED
TransactionStatus.PENDING
TransactionStatus.REFUNDED
```

Benefits:

* Compile-time type safety.
* Prevents invalid status values.
* Easier refactoring.
* Improved readability and consistency across entities, DTOs and service layer.

---

### Customer Segment Enum

Extended the existing `CustomerSegment` enum by adding:

```java
PREMIUM
```

This allows the application to support an additional customer category while maintaining the benefits of enum-based validation and type safety.

---

### Error Handling Improvement

After introducing custom exceptions, missing customers and transactions now generate meaningful business exceptions instead of generic runtime exceptions.

Example:

```text
Cliente no encontrado con id: 4
```

This makes troubleshooting easier and prepares the application for future centralized exception handling and proper HTTP status code responses.

### Possible modification in bussiness logic
- The original implementation only returned transaction counts for VIP customers. Since the endpoint is a customer summary and no business requirement restricted transaction visibility by segment, I updated the implementation to return the transaction count consistently for all customer types (STANDARD, PREMIUM and VIP).
- During testing I observed that refund transactions are included in the customer's transaction count because refunds are stored as independent transactions linked to the original transaction. I left the behavior unchanged since the challenge requirements do not specify whether refunded transactions should be excluded from summary calculations.