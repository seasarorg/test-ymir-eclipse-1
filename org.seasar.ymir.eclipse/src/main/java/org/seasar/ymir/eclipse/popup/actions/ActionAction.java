package org.seasar.ymir.eclipse.popup.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.seasar.eclipse.common.util.AdaptableUtil;

public class ActionAction implements IObjectActionDelegate, IMenuCreator {
    boolean fillMenu;

    private IProject project;

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    public void run(IAction action) {
    }

    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            fillMenu = true;
            Object o = ((IStructuredSelection) selection).getFirstElement();
            project = AdaptableUtil.toProject(o);
            if (project == null) {
                IResource r = AdaptableUtil.toResource(o);
                if (r != null) {
                    project = r.getProject();
                }
            }
        }
    }

    public void dispose() {
    }

    public Menu getMenu(Control parent) {
        return null;
    }

    public Menu getMenu(Menu parent) {
        Menu menu = new Menu(parent);

        /**
         * Add listener to re-populate the menu each time it is shown because MenuManager.update(boolean, boolean) doesn't
         * dispose pull-down ActionContribution items for each popup menu.
         */
        menu.addMenuListener(new MenuAdapter() {
            public void menuShown(MenuEvent e) {
                if (fillMenu) {
                    Menu m = (Menu) e.widget;

                    for (MenuItem item : m.getItems()) {
                        item.dispose();
                    }

//                    IMenuManager mgr = new MenuManager("#action");
//                    mgr.add(new GroupMarker(AbstractMavenMenuCreator.NEW));
//                    mgr.insertAfter(AbstractMavenMenuCreator.NEW, new GroupMarker(AbstractMavenMenuCreator.UPDATE));
//                    mgr.insertAfter(AbstractMavenMenuCreator.UPDATE, new GroupMarker(AbstractMavenMenuCreator.OPEN));
//                    mgr.insertAfter(AbstractMavenMenuCreator.OPEN, new GroupMarker(AbstractMavenMenuCreator.NATURE));
//                    mgr.insertAfter(AbstractMavenMenuCreator.NATURE, new GroupMarker(AbstractMavenMenuCreator.IMPORT));
//
//                    for (AbstractMavenMenuCreator creator : getCreators()) {
//                        creator.createMenu(mgr);
//                    }
//
//                    for (IContributionItem item : mgr.getItems()) {
//                        item.fill(m, -1);
//                    }

                    fillMenu = false;
                }
            }
        });

        return menu;
    }
}
