```
com.example.urlshortener
│
├── UrlShortenerApplication.java        // main entry point
│
├── config/                             // cross-cutting configuration
│   ├── SecurityConfig.java
│   ├── SwaggerConfig.java
│   ├── WebConfig.java
│   └── AsyncConfig.java
│
├── common/                             // shared across features
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ResourceNotFoundException.java
│   │   └── ApiError.java
│   ├── util/
│   │   ├── DateUtils.java
│   │   └── HashUtils.java
│   └── constants/
│       └── AppConstants.java
│
├── url/                                 // feature module: URL shortening
│   ├── controller/
│   │   └── UrlController.java
│   ├── service/
│   │   ├── UrlService.java              // interface
│   │   └── impl/
│   │       └── UrlServiceImpl.java
│   ├── repository/
│   │   └── UrlRepository.java
│   ├── entity/
│   │   └── UrlMapping.java
│   ├── dto/
│   │   ├── UrlRequestDto.java
│   │   └── UrlResponseDto.java
│   └── mapper/
│       └── UrlMapper.java
│
├── analytics/                           // feature module: click analytics
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── user/                                // feature module: auth/user mgmt
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
└── security/
├── JwtTokenProvider.java
├── JwtAuthFilter.java
└── CustomUserDetailsService.java
```
