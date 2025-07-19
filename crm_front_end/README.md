# CRM Frontend - Angular Application

A modern, responsive Angular frontend application for the Legacy CRM Java backend. This application provides a beautiful and intuitive user interface for managing orders, shipments, and files.

## Features

- **Modern UI/UX** - Clean, responsive design with smooth animations
- **Authentication** - Secure login with session management
- **Order Management** - Full CRUD operations for customer orders
- **Dashboard** - Overview with key metrics and recent data
- **Responsive Design** - Works seamlessly on desktop, tablet, and mobile
- **Real-time Updates** - Live data synchronization with the backend

## Technology Stack

- **Angular 20** - Latest version with standalone components
- **TypeScript** - Type-safe development
- **SCSS** - Advanced styling with CSS preprocessor
- **RxJS** - Reactive programming for state management
- **HTTP Client** - RESTful API communication

## Prerequisites

- Node.js 18+ 
- npm or yarn package manager
- Legacy CRM Java backend running on port 8089

## Quick Start

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Start the development server:**
   ```bash
   npm start
   ```

3. **Open your browser:**
   Navigate to `http://localhost:4200`

4. **Login with demo credentials:**
   - Username: `admin`, Password: `password`
   - Username: `user1`, Password: `password`
   - Username: `user2`, Password: `password`

## Project Structure

```
src/
├── app/
│   ├── components/           # UI Components
│   │   ├── login/           # Authentication component
│   │   ├── dashboard/       # Main dashboard
│   │   └── orders/          # Order management
│   ├── models/              # TypeScript interfaces
│   │   ├── user.model.ts
│   │   ├── order.model.ts
│   │   ├── file.model.ts
│   │   └── shipment.model.ts
│   ├── services/            # API services
│   │   ├── auth.service.ts
│   │   ├── order.service.ts
│   │   ├── file.service.ts
│   │   └── shipment.service.ts
│   ├── app.component.ts     # Root component
│   ├── app.routes.ts        # Routing configuration
│   └── app.config.ts        # Application configuration
├── styles.scss              # Global styles
└── main.ts                  # Application entry point
```

## API Integration

The frontend communicates with the Java backend through RESTful APIs:

- **Base URL:** `http://localhost:8089`
- **Authentication:** `/login`
- **Orders:** `/orders`
- **Files:** `/files`
- **Shipments:** `/ext/shipment`

## Available Scripts

- `npm start` - Start development server
- `npm run build` - Build for production
- `npm run test` - Run unit tests
- `npm run lint` - Run linting

## Features Overview

### Authentication
- Secure login with username/password
- Session token management
- Automatic logout on session expiry
- Protected routes

### Dashboard
- Overview of key metrics
- Recent orders and shipments
- Quick navigation to main features
- Real-time data updates

### Order Management
- View all orders in a responsive grid
- Create new orders with multiple items
- Edit existing orders
- Delete orders with confirmation
- Order status tracking

### Responsive Design
- Mobile-first approach
- Adaptive layouts for all screen sizes
- Touch-friendly interface
- Optimized for tablets and desktops

## Development

### Adding New Components

1. Create component files:
   ```bash
   ng generate component components/your-component
   ```

2. Add to routing in `app.routes.ts`

3. Import in parent component

### Styling Guidelines

- Use SCSS for component styles
- Follow BEM methodology for CSS classes
- Use CSS Grid and Flexbox for layouts
- Implement responsive breakpoints
- Use CSS custom properties for theming

### State Management

- Use RxJS BehaviorSubject for reactive state
- Services handle API communication
- Components subscribe to service observables
- Local component state for UI interactions

## Production Build

1. **Build the application:**
   ```bash
   npm run build
   ```

2. **Serve the built files:**
   ```bash
   npm run serve:prod
   ```

3. **Deploy to your web server**

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Contributing

1. Follow Angular style guide
2. Write unit tests for new features
3. Ensure responsive design
4. Update documentation

## Troubleshooting

### Common Issues

1. **CORS Errors:** Ensure backend CORS is configured
2. **API Connection:** Verify backend is running on port 8089
3. **Build Errors:** Clear node_modules and reinstall
4. **Styling Issues:** Check SCSS compilation

### Development Tips

- Use Angular DevTools for debugging
- Monitor network tab for API calls
- Check console for TypeScript errors
- Use browser responsive mode for testing

## License

This project is part of the CRM system demonstration.
