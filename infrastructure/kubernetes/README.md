# Kubernetes manifests (starter)

These are minimal, production-shaped manifests to show the platform is Kubernetes-compatible.
They are a starting point, not a hardened production deployment.

Apply order:

```bash
kubectl apply -f namespace.yaml
kubectl apply -f secrets.example.yaml      # edit real secrets first (do NOT commit real values)
kubectl apply -f postgres.yaml
kubectl apply -f redis.yaml
kubectl apply -f backend.yaml
kubectl apply -f frontend.yaml
```

Before production:
- Replace the example Secret with a real one from your secret manager (do not commit secrets).
- Use a managed PostgreSQL and Redis instead of in-cluster single replicas, or add
  StatefulSets with proper storage/backup.
- Add an Ingress + TLS, resource limits tuning, HPA, and network policies.
