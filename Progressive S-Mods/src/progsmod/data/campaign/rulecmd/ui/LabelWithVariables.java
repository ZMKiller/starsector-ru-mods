package progsmod.data.campaign.rulecmd.ui;

import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;

import java.awt.Color;

public class LabelWithVariables<T> implements UIElement {

    private String text;
    private T[] vars;
    private String[] highlights;
    private Color highlightColor;
    private Alignment alignment;

    public LabelAPI label;

    @SafeVarargs
    public LabelWithVariables(String text, Color highlightColor, Alignment alignment, T... vars) {
        this.text = text;
        this.vars = vars;
        this.highlightColor = highlightColor;
        highlights = new String[vars.length];
        for (int i = 0; i < vars.length; i++) {
            highlights[i] = vars[i].toString();
        }
        this.alignment = alignment;
    }

    public T getVar(int index) {
        return index < vars.length ? vars[index] : null;
    }

    public void changeVar(int index, T newVar) {
        if (index >= vars.length) {
            return;
        }
        vars[index] = newVar;
        highlights[index] = newVar.toString();
        label.setText(String.format(text, vars));
        label.setHighlight(highlights);
    }

    public void changeHighlightColor(Color newColor) {
        label.setHighlightColor(newColor);
    }

    @Override
    public void init(TooltipMakerAPI tooltip) {
        label = tooltip.addPara(String.format(text, vars), 0f);
        label.setHighlight(highlights);
        label.setHighlightColor(highlightColor);
        label.setAlignment(alignment);
    }
}