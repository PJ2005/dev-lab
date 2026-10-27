# Campus Equipment Booking Service

## Build and test
Run automated tests - `mvn clean test`

Package application - `mvn clean package`

## Run
`mvn spring-boot:run` — port 8080.

## Endpoints
- `GET /api/equipment`
- `GET /api/equipment/{id}`
- `POST /api/bookings`
- `GET /api/bookings/{id}`
- `POST /api/bookings/{id}/cancel`
- `GET /actuator/health`

Inventory is in memory and resets on restart. No database is required.

## Docker

Build the application JAR - `mvn clean package`
Build the Docker image with the required tag - `docker build -t campus-equipment-booking:set-f .`
Run the container on port 8080 - `docker run -d --name campus-booking -p 8080:8080 campus-equipment-booking:set-f`
Verify the image and running container - `docker images campus-equipment-booking \ docker ps`

## Kubernetes with MiniKube

Start Minikube - `MiniKube Start`
Load the local Docker image into Minikube - `minikube image load campus-equipment-booking:set-f`

Apply the Deployment and Service - `kubectl apply -f deployment.yaml`
`kubectl apply -f service.yaml`

Check Kubernetes resources - 
`kubectl get deployments`
`kubectl get pods`
`kubectl get services`

## Health Check

Forward local port 8080 to the Kubernetes Service - `kubectl port-forward service/campus-booking-service 8080:8080`

Keep the port-forward command running and open another terminal - `curl http://localhost:8080/actuator/health`

## Scale to Two Replicas

`kubectl scale deployment campus-booking --replicas=2`
`kubectl get deployments`
`kubectl get pods`

## Changes

### Booking Capacity Validation
The capacity-validation defect allowed a booking quantity one unit greater than the available inventory because the validation condition added one to the available quantity.

The condition was corrected to reject requests when the requested quantity exceeds the available quantity. Positive quantities are still required.

Automated tests verify that a request equal to available inventory is accepted, a request exceeding inventory is rejected, and rejected requests do not reduce inventory.

### Booking Cancellation State Consistency
Only confirmed bookings may be cancelled. A successful cancellation changes the booking status to CANCELLED and restores the reserved inventory exactly once. Repeated cancellation attempts are rejected and do not restore inventory again.

Automated tests verify cancellation status, restored inventory, and rejection of repeated cancellation attempts.
