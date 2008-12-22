package org.seasar.ymir.eclipse.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.seasar.eclipse.common.util.AdaptableUtil;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ProjectRelative;

public class ActionAction implements IObjectActionDelegate, IMenuCreator {
    private ISelection selection;

    private IProject project;

    private boolean fillMenu;

    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    public void run(IAction action) {
    }

    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
        if (selection instanceof IStructuredSelection) {
            Object o = ((IStructuredSelection) selection).getFirstElement();
            project = AdaptableUtil.toProject(o);
            if (project == null) {
                IResource r = AdaptableUtil.toResource(o);
                if (r != null) {
                    project = r.getProject();
                }
            }

            fillMenu = true;
            action.setMenuCreator(this);
            action.setEnabled(true);
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

                    IMenuManager mgr = new MenuManager("#action");
                    createMenu(mgr);
                    for (IContributionItem item : mgr.getItems()) {
                        item.fill(m, -1);
                    }

                    fillMenu = false;
                }
            }
        });

        return menu;
    }

    void createMenu(IMenuManager mgr) {
        ProjectRelative relative = Activator.getDefault().getProjectRelative(project);
        for (org.seasar.ymir.vili.model.Action action : relative.getActions().getActions()) {
            mgr.add(getAction(new ActionActionDelegate(action, project), getActionId(action), action.getName()));
        }
    }

    private String getActionId(org.seasar.ymir.vili.model.Action action) {
        return Activator.PLUGIN_ID + ".action." + action.getGroupId() + ":" + action.getArtifactId() + ":"
                + action.getVersion() + ":" + action.getActionId();
    }

    protected IAction getAction(IActionDelegate delegate, String id, String text) {
        return getAction(delegate, id, text, (ImageDescriptor) null);
    }

    protected IAction getAction(IActionDelegate delegate, String id, String text, String image) {
        return getAction(delegate, id, text, Activator.getImageDescriptor(image));
    }

    protected IAction getAction(IActionDelegate delegate, String id, String text, ImageDescriptor image) {
        ActionProxy action = new ActionProxy(id, text, delegate);
        if (image != null) {
            action.setImageDescriptor(image);
        }
        return action;
    }

    class ActionProxy extends Action {
        private IActionDelegate action;

        public ActionProxy(String id, String text, IActionDelegate action) {
            super(text);
            this.action = action;
            setId(id);
        }

        public ActionProxy(String id, String text, IActionDelegate action, int style) {
            super(text, style);
            this.action = action;
            setId(id);
        }

        public void run() {
            action.selectionChanged(this, selection);
            action.run(this);
        }
    }

}
