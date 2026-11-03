package fr.ekod.boutique;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Calcule les frais de port d'une commande selon le poids du colis et la zone de livraison.
 * Le port est offert à partir de 60,00 € de commande, en France uniquement.
 */
public class FraisDePort {

    private static final int POIDS_MAXIMUM_GRAMMES = 30_000;
    private static final Montant SEUIL_FRANCO = Montant.euros(60.00);

    private static final List<Tranche> TRANCHES = List.of(
            tranche(250, 3.90, 8.50, 14.00),
            tranche(1_000, 5.90, 12.50, 22.00),
            tranche(5_000, 8.90, 19.90, 39.00),
            tranche(POIDS_MAXIMUM_GRAMMES, 14.90, 34.90, 79.00));

    private record Tranche(int poidsMaximumGrammes, Map<Zone, Montant> tarifs) {
    }

    private static Tranche tranche(int poidsMaximumGrammes, double france, double europe, double monde) {
        Map<Zone, Montant> tarifs = new EnumMap<>(Zone.class);
        tarifs.put(Zone.FRANCE, Montant.euros(france));
        tarifs.put(Zone.EUROPE, Montant.euros(europe));
        tarifs.put(Zone.MONDE, Montant.euros(monde));
        return new Tranche(poidsMaximumGrammes, tarifs);
    }

    public Montant calculer(int poidsGrammes, Zone zone, Montant montantCommande) {
        if (poidsGrammes <= 0) {
            throw new IllegalArgumentException("poids invalide");
        }
        if (poidsGrammes > POIDS_MAXIMUM_GRAMMES) {
            throw new IllegalArgumentException("colis trop lourd");
        }
        if (beneficieDuFranco(zone, montantCommande)) {
            return Montant.ZERO;
        }
        return trancheDuPoids(poidsGrammes).tarifs().get(zone);
    }

    private boolean beneficieDuFranco(Zone zone, Montant montantCommande) {
        return zone == Zone.FRANCE && montantCommande.estSuperieurOuEgalA(SEUIL_FRANCO);
    }

    private Tranche trancheDuPoids(int poidsGrammes) {
        return TRANCHES.stream()
                .filter(tranche -> poidsGrammes <= tranche.poidsMaximumGrammes())
                .findFirst()
                .orElseThrow();
    }
}
