package com.termux.menu.ui;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.termux.menu.R;
import com.termux.menu.utils.FileUtils;

public class EditorController {
    private View mRootView;
    private EditText mEditorText;
    private TextView mFilePath;
    private TextView mSaveBtn;
    private PageController mPageController;
    private String mCurrentPath;

    public EditorController(View rootView, PageController pageController) {
        mRootView = rootView;
        mPageController = pageController;
        mEditorText = rootView.findViewById(R.id.editor_text);
        mFilePath = rootView.findViewById(R.id.editor_filepath);
        mSaveBtn = rootView.findViewById(R.id.editor_save_btn);

        mSaveBtn.setOnClickListener(v -> saveFile());
    }

    public void openFile(String path) {
        mCurrentPath = path;
        mFilePath.setText(path);
        mPageController.setTitle("编辑器");

        String content = FileUtils.readFile(path);
        if (content != null) {
            mEditorText.setText(content);
        } else {
            mEditorText.setText("");
            mEditorText.setHint("文件不存在或为空: " + path);
        }

        mPageController.showPage(PageController.PAGE_EDITOR);
        mEditorText.requestFocus();
    }

    public void openNewFile(String path, String defaultContent) {
        mCurrentPath = path;
        mFilePath.setText(path);
        mPageController.setTitle("编辑器 - 新建");
        mEditorText.setText(defaultContent != null ? defaultContent : "");
        mPageController.showPage(PageController.PAGE_EDITOR);
        mEditorText.requestFocus();
    }

    private void saveFile() {
        if (mCurrentPath == null || mCurrentPath.isEmpty()) {
            Toast.makeText(mRootView.getContext(), "未指定文件路径", Toast.LENGTH_SHORT).show();
            return;
        }
        String content = mEditorText.getText().toString();
        boolean ok = FileUtils.writeFile(mCurrentPath, content);
        Toast.makeText(mRootView.getContext(),
            ok ? "已保存: " + mCurrentPath : "保存失败", Toast.LENGTH_SHORT).show();
    }

    public String getCurrentPath() {
        return mCurrentPath;
    }
}
