# Great Britain Energy Mix Application

Web application for displaying the current and forecasted energy mix of Great Britain and calculating the optimal electric vehicle charging window based on the share of clean energy.

## Technologies

### Backend
- Java 17
- Spring Boot
- Maven
- Carbon Intensity API

### Frontend
- React
- TypeScript
- Vite
- Recharts

## External API

The application uses the public Carbon Intensity API:

GET https://api.carbonintensity.org.uk/generation/{from}/{to}

The API returns Great Britain energy generation mix data in 30-minute intervals.

Clean energy is calculated as the sum of:

- biomass
- nuclear
- hydro
- wind
- solar

## Backend endpoints

### Test endpoint


Returns a test message confirming that the backend is running.

### Energy mix endpoint

Returns average energy mix data for three days: today, tomorrow and the day after tomorrow.

The response contains:
- date,
- number of available 30-minute intervals,
- average percentage share of each energy source,
- average clean energy percentage.

### Charging window endpoint


The `hours` parameter must be a full number from 1 to 6.

The endpoint returns:
- start date and time,
- end date and time,
- average clean energy percentage in the selected time window.


