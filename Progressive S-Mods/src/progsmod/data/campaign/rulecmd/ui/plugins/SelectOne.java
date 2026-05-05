package progsmod.data.campaign.rulecmd.ui.plugins;

import java.util.List;

import progsmod.data.campaign.rulecmd.ui.Button;

public class SelectOne<T extends Button> extends Selector<T> {

    public int getSelectedIndex() {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isSelected()) {
                return i;
            }
        }
        return -1;
    }

    public void disableItem(int index) {
        items.get(index).disable();
    }

    @Override
    public void init(List<T> items) {
        super.init(items);
    }

    @Override
    protected void onSelected(int index) {
        // Deselect every other item
        for (int i = 0; i < items.size(); i++) {
            if (i == index) {
                continue;
            }
            forceDeselect(i);
        }
    }

    @Override
    protected void onDeselected(int index) {}
}
