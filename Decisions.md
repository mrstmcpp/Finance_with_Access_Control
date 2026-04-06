## Architecture & Design Decisions

- **Layered Architecture:** Followed controller → service → repository separation for clean, maintainable, and testable code.  
- **Stateless Authentication:** Implemented scalable auth using JWT; avoided session storage for better horizontal scaling.  
- **Route-Level Authorization:** Enforced authorization at HTTP layer for improved API security.  
- **Audit-Friendly User Management:** Stored promotion metadata (`promotedByUserId`, `promotionDate`) for admin traceability.  
- **Soft Delete Strategy:** Used `deleted` flags in transactions to ensure recoverability and audit compliance.  
- **Dynamic Querying:** Enabled flexible filters and aggregation pipelines using MongoDB.  
- **Global Exception Handling:** Centralized exception handler for consistent API error responses.  
- **Rate Limiting (MVP):** Implemented in-memory rate limiter for basic traffic control.  
- **Automated Auditing:** Leveraged base model + Mongo auditing for consistent creation/update tracking.  
