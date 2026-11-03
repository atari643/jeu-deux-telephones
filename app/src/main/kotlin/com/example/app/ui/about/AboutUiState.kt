package com.example.app.ui.about

/** Etat COMPLET de l'ecran, immuable. L'ecran n'affiche que ce qui est ici. */
data class AboutUiState(val version: String = "1.0")

/** Ecran statique : aucun evenement, le retour est gere par la navigation. */
sealed interface AboutEvent
