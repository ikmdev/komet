package au.csiro.ontology.model;

public class FunctionalFeature extends Axiom {

    private Feature feature;

    public FunctionalFeature() {
    }

    public FunctionalFeature(Feature feature) {
        this.feature = feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public Feature getFeature() {
        return feature;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((feature == null) ? 0 : feature.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FunctionalFeature other = (FunctionalFeature) obj;
        if (feature == null) {
            if (other.feature != null)
                return false;
        } else if (!feature.equals(other.feature))
            return false;
        return true;
    }

    @Override
    public int compareTo(Axiom o) {
        if (!(o instanceof FunctionalFeature)) {
            return 1;
        } else {
            return 0;
        }
    }

}
