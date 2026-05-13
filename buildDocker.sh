#!/bin/sh
version="0.0.1-test"
docker build -f admin-management/Dockerfile ./admin-management -t restaurant-api/admin-management:$version --platform linux/amd64

docker build -f api-gateway/Dockerfile ./api-gateway -t restaurant-api/api-gateway:$version --platform linux/amd64

docker build -f inventory-management/Dockerfile ./inventory-management -t restaurant-api/inventory-management:$version --platform linux/amd64
echo 'Se finalizo la construccion de imagenes'