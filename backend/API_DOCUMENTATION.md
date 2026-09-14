# Smart Cataloging Mobile Application - Backend API

## Base URL
http://localhost:8080

## Artisan APIs
POST   /api/artisans
GET    /api/artisans
GET    /api/artisans/{id}
PUT    /api/artisans/{id}
DELETE /api/artisans/{id}

## Product APIs
POST   /api/products?artisanId={artisanId}
GET    /api/products
GET    /api/products/{id}
PUT    /api/products/{id}
DELETE /api/products/{id}

### Product Search
GET /api/products/search?name={name}
GET /api/products/search?category={category}
GET /api/products/search?material={material}
GET /api/products/search?type={type}

## Category APIs
POST   /api/categories
GET    /api/categories
GET    /api/categories/{id}
PUT    /api/categories/{id}
DELETE /api/categories/{id}

## Market APIs
POST   /api/markets
GET    /api/markets
GET    /api/markets/{id}
PUT    /api/markets/{id}
DELETE /api/markets/{id}

## Market-Product APIs
POST   /api/market-products?marketId={marketId}&productId={productId}
GET    /api/market-products
GET    /api/market-products/{id}
PUT    /api/market-products/{id}
DELETE /api/market-products/{id}

## Order APIs
POST   /api/orders
GET    /api/orders
GET    /api/orders/{id}
PUT    /api/orders/{id}
DELETE /api/orders/{id}

## User APIs
POST /api/users
GET  /api/users
GET  /api/users/{id}
PUT  /api/users/{id}
DELETE /api/users/{id}

## Login
POST /api/users/login?mobileNumber={mobileNumber}&password={password}

## AI APIs

### Product Analysis
POST /api/ai/analyze

### Image Analysis
POST /api/ai/analyze-image

### Speech-to-Text
POST /api/ai/speech-to-text

### Price Estimation
POST /api/ai/estimate-price

### Feature Extraction
POST /api/ai/extract-features

### Complete Product Analysis
POST /api/ai/analyze-product

### Market Recommendation
POST /api/ai/recommend-markets

### Create Product From Image
POST /api/ai/create-product-from-image

## Technology Stack
- Java 23
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- BCrypt
- H2 Database
- Maven
