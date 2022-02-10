/* create by Spreadst */

package com.sprd.calendar;

import com.android.calendar.R;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/* SPRD: bug290535 2014-03-19 New event title can not be too long */
public class SprdTextWatcher implements TextWatcher {

    private Context mContext;
    private TextView mEditText;
    private int mMax;
    private View mAddEventView;
    private String mCostStr;
    private Toast textLenghtToast;

    /* SPRD : strengthen the title length control */
    public SprdTextWatcher(Context c, TextView textview, int max) {
        this.mContext = c;
        this.mEditText = textview;
        this.mMax = max;
    }

    public SprdTextWatcher(Context c, TextView textview, View view, int max) {
        this.mContext = c;
        this.mEditText = textview;
        this.mAddEventView = view;
        this.mMax = max;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        mCostStr = s.toString();
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (mAddEventView != null) {
            mAddEventView.setEnabled(s.length() > 0);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        showMessage(s.toString(), mCostStr, mEditText, mMax);
    }

    /**
     * if new string's length greater than max, the string revert old.
     *
     * @param text new string
     * @param costStr old string
     * @param editText input view
     * @param max Max length
     */
    /* SPRD: Add 20130829 of bug 209021,limit text length @{ */
    public void showMessage(String text, String costStr, TextView textview, int max) {
        /* SPRD:modify for Bug 744257 1027842 @{ */
        /*
        if (textLenghtToast != null) {
            textLenghtToast.cancel();
        }
        textLenghtToast = Toast.makeText(mContext, R.string.title_too_long,
                Toast.LENGTH_SHORT);
        */
        textview.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
                max) {
            @Override
            /*
             * here are some explanation of some parameters source:the input
             * text start:start position end:end position dest:originally
             * displayed content dstart:current start position dend:current end
             * position
             */
            public CharSequence filter(CharSequence source, int start, int end,
                    Spanned dest, int dstart, int dend) {
                int remainLength = max - (dest.length() - (dend - dstart));
                if (remainLength <= 0) {
                    /*UNISOC: Modify for bug 1147618 @{*/
                    if (textLenghtToast != null) {
                        textLenghtToast.cancel();
                    }
                    /*UNISOC: Modify for bug 1027842 @{*/
                    textLenghtToast = Toast.makeText(mContext, R.string.title_too_long,Toast.LENGTH_SHORT);
                    /* @} */
                    textLenghtToast.show();
                    /* @} */
                    return "";// do not change the original character
                } else if (remainLength >= end - start) {
                    return null; // keep original
                } else {
                    // Additional character length is less than the length of
                    // the remaining,
                    // Only add additional characters are part of it
                    remainLength += start;
                    if (Character.isHighSurrogate(source
                            .charAt(remainLength - 1))) {
                        --remainLength;
                        if (remainLength == start) {
                            return "";
                        }
                    }
                    return source.subSequence(start, remainLength);
                }
            }
        } });
    }
}
