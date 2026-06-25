package com.termux.menu.ui;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.termux.menu.R;

public class PageController {
    public static final int PAGE_MENU = 0;
    public static final int PAGE_EDITOR = 1;
    public static final int PAGE_FILEBROWSER = 2;
    public static final int PAGE_SETTINGS = 3;
    public static final int PAGE_TERMINAL = 4;

    private View mPageMenu;
    private View mPageEditor;
    private View mPageFileBrowser;
    private View mPageSettings;
    private View mPageTerminal;
    private ImageView mBackBtn;
    private TextView mTitleText;
    private int mCurrentPage = PAGE_MENU;
    private String mDefaultTitle = "GavinFloat";

    public interface PageChangeListener {
        void onPageChanged(int page);
    }

    private PageChangeListener mListener;

    public PageController(View rootView, PageChangeListener listener) {
        mPageMenu = rootView.findViewById(R.id.page_menu);
        mPageEditor = rootView.findViewById(R.id.page_editor);
        mPageFileBrowser = rootView.findViewById(R.id.page_filebrowser);
        mPageSettings = rootView.findViewById(R.id.page_settings);
        mPageTerminal = rootView.findViewById(R.id.page_terminal);
        mBackBtn = rootView.findViewById(R.id.header_back);
        mTitleText = rootView.findViewById(R.id.header_title);
        mListener = listener;

        mBackBtn.setOnClickListener(v -> goBack());
        showPage(PAGE_MENU);
    }

    public void showPage(int page) {
        mCurrentPage = page;
        mPageMenu.setVisibility(page == PAGE_MENU ? View.VISIBLE : View.GONE);
        mPageEditor.setVisibility(page == PAGE_EDITOR ? View.VISIBLE : View.GONE);
        mPageFileBrowser.setVisibility(page == PAGE_FILEBROWSER ? View.VISIBLE : View.GONE);
        mPageSettings.setVisibility(page == PAGE_SETTINGS ? View.VISIBLE : View.GONE);
        mPageTerminal.setVisibility(page == PAGE_TERMINAL ? View.VISIBLE : View.GONE);

        if (page == PAGE_MENU) {
            mBackBtn.setVisibility(View.GONE);
            mTitleText.setText(mDefaultTitle);
        } else {
            mBackBtn.setVisibility(View.VISIBLE);
        }

        if (mListener != null) mListener.onPageChanged(page);
    }

    public void setTitle(String title) {
        mTitleText.setText(title);
    }

    public void setDefaultTitle(String title) {
        mDefaultTitle = title;
        if (mCurrentPage == PAGE_MENU) mTitleText.setText(title);
    }

    public void goBack() {
        showPage(PAGE_MENU);
    }

    public int getCurrentPage() {
        return mCurrentPage;
    }
}
