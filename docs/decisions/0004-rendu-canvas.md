# 0004 - Rendu avec Compose Canvas, sans moteur de jeu externe

**Décision.** `Canvas` Compose + boucle `withFrameNanos` dans la Route ; le ViewModel appelle le moteur
à pas fixe ; l'écran ne fait que dessiner l'état.
**Pourquoi.** Suffisant pour un jeu 2D simple à 60 images/s, reste dans la stack figée, testable avec
Robolectric et Roborazzi (snapshots d'états figés).
**Rejeté.** libGDX (nouvelle dépendance native, incompatible avec les tests JVM, plantage connu en pause
dans une app Compose), SurfaceView (inutile tant que la scène reste simple).
