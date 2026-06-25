package com.termux.menu.ui;

import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.termux.menu.R;
import com.termux.menu.termux.TermuxCommandHelper;

public class TerminalOutputController {
    private View mRootView;
    private TextView mOutputText;
    private TextView mCommandText;
    private ScrollView mScrollView;
    private TermuxCommandHelper mCmdHelper;
    private PageController mPageController;

    public TerminalOutputController(View rootView, TermuxCommandHelper cmdHelper, PageController pageController) {
        mRootView = rootView;
        mCmdHelper = cmdHelper;
        mPageController = pageController;
        mOutputText = rootView.findViewById(R.id.terminal_output);
        mCommandText = rootView.findViewById(R.id.terminal_command);
        mScrollView = rootView.findViewById(R.id.terminal_scroll);
    }

    public void executeAndShow(String command) {
        mPageController.setTitle("终端输出");
        mCommandText.setText("$ " + command);
        mOutputText.setText("执行中...");
        mPageController.showPage(PageController.PAGE_TERMINAL);

        mCmdHelper.executeAndCapture(command, output -> {
            mOutputText.setText(output);
            if (mScrollView != null) {
                mScrollView.post(() -> mScrollView.fullScroll(ScrollView.FOCUS_UP));
            }
        });
    }
}
