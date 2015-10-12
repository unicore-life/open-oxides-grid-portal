package pl.edu.icm.oxides.open.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Oxide {
    private String name;
    private String formula;
    private String spacegroup;

    @JsonCreator
    public Oxide(@JsonProperty("name") String name,
                 @JsonProperty("formula") String formula,
                 @JsonProperty("spacegroup") String spacegroup) {
        this.name = name;
        this.formula = formula;
        this.spacegroup = spacegroup;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public void setSpacegroup(String spacegroup) {
        this.spacegroup = spacegroup;
    }

    public String getName() {
        return name;
    }

    public String getFormula() {
        return formula;
    }

    public String getSpacegroup() {
        return spacegroup;
    }

    @Override
    public String toString() {
        return String.format("Oxide{name='%s', formula='%s', spacegroup='%s'}", name, formula, spacegroup);
    }
}
