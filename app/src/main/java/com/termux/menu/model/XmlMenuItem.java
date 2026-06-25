package com.termux.menu.model;

public class XmlMenuItem {
    private String name;
    private String clickAction;
    private String icon;
    private String packageName;
    private String intentData;
    private boolean autoRunShell;
    private boolean dialogConfirm;
    private String dialogTitle;
    private String dialogMessage;
    private String listTitle;
    private String activityTitle;

    public XmlMenuItem(String name, String clickAction, String icon,
                       boolean autoRunShell, String packageName,
                       boolean dialogConfirm, String dialogTitle,
                       String dialogMessage, String intentData,
                       String listTitle, String activityTitle) {
        this.name = name;
        this.clickAction = clickAction;
        this.icon = icon;
        this.packageName = packageName;
        this.autoRunShell = autoRunShell;
        this.intentData = intentData;
        this.dialogConfirm = dialogConfirm;
        this.dialogTitle = dialogTitle;
        this.dialogMessage = dialogMessage;
        this.listTitle = listTitle;
        this.activityTitle = activityTitle;
    }

    public String getName() { return name; }
    public String getClickAction() { return clickAction; }
    public String getIcon() { return icon; }
    public String getPackageName() { return packageName; }
    public String getIntentData() { return intentData; }
    public boolean isAutoRunShell() { return autoRunShell; }
    public boolean isDialogConfirm() { return dialogConfirm; }
    public String getDialogTitle() { return dialogTitle; }
    public String getDialogMessage() { return dialogMessage; }
    public String getListTitle() { return listTitle; }
    public String getActivityTitle() { return activityTitle; }
}
