package com.sprd.calendar.newmonth.base.task;

import android.content.Context;
import android.os.AsyncTask;

import com.sprd.calendar.newmonth.listener.OnTaskFinishedListener;

public abstract class BaseAsyncTask<T> extends AsyncTask<Void, Void, T> {

    protected Context mContext;
    protected OnTaskFinishedListener<T> mOnTaskFinishedListener;
    protected boolean mNeedUpdate;

    public BaseAsyncTask(Context context,
            OnTaskFinishedListener<T> onTaskFinishedListener) {
        mContext = context;
        mOnTaskFinishedListener = onTaskFinishedListener;
    }

    @Override
    protected abstract T doInBackground(Void... params);

    @Override
    protected void onPostExecute(T data) {
        super.onPostExecute(data);
        if (mOnTaskFinishedListener != null) {
            mOnTaskFinishedListener.setNeedUpdate(mNeedUpdate);
            mOnTaskFinishedListener.onTaskFinished(data);
        }
    }
}
