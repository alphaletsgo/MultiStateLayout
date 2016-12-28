package cn.isif.msl;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by dell on 2016/12/23-9:48.
 */

public class MultiStateLayout extends FrameLayout {
    private static final String TAG_EMPTY = "empty";
    private static final String TAG_LOADING = "loading";
    private static final String TAG_ERROR = "error";
    private static final String TAG_OTHER = "other";

    public static final int VIEW_STATE_UNKNOWN = -1;
    public static final int VIEW_STATE_CONTENT = 0;
    public static final int VIEW_STATE_ERROR = 1;
    public static final int VIEW_STATE_EMPTY = 2;
    public static final int VIEW_STATE_LOADING = 3;
    public static final int VIEW_STATE_OTHER = 4;
    private int mLoadingViewResId;
    private int mEmptyViewResId;
    private int mErrorViewResId;
    private int mOtherViewResId;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({VIEW_STATE_UNKNOWN, VIEW_STATE_CONTENT, VIEW_STATE_ERROR, VIEW_STATE_EMPTY, VIEW_STATE_LOADING, VIEW_STATE_OTHER})
    public @interface ViewState {
    }

    private LayoutInflater mInflater;
    private View mContentView;
    private View mLoadingView;
    private View mErrorView;
    private View mEmptyView;
    private View mOtherView;

    @Nullable
    private StateListener mListener;

    @ViewState
    private int mViewState = VIEW_STATE_UNKNOWN;

    public MultiStateLayout(Context context) {
        this(context, null);
    }

    public MultiStateLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public MultiStateLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        mInflater = LayoutInflater.from(getContext());
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MultiStateLayout);
        mLoadingViewResId = a.getResourceId(R.styleable.MultiStateLayout_msl_loadingView, -1);
        mEmptyViewResId = a.getResourceId(R.styleable.MultiStateLayout_msl_emptyView, -1);
        mErrorViewResId = a.getResourceId(R.styleable.MultiStateLayout_msl_errorView, -1);
        mOtherViewResId = a.getResourceId(R.styleable.MultiStateLayout_msl_otherView, -1);
        int viewState = a.getInt(R.styleable.MultiStateLayout_msl_viewState, VIEW_STATE_CONTENT);
        switch (viewState) {
            case VIEW_STATE_CONTENT:
                mViewState = VIEW_STATE_CONTENT;
                break;
            case VIEW_STATE_ERROR:
                mViewState = VIEW_STATE_ERROR;
                break;
            case VIEW_STATE_EMPTY:
                mViewState = VIEW_STATE_EMPTY;
                break;
            case VIEW_STATE_LOADING:
                mViewState = VIEW_STATE_LOADING;
                break;
            case VIEW_STATE_OTHER:
                mViewState = VIEW_STATE_OTHER;
                break;
            case VIEW_STATE_UNKNOWN:
            default:
                mViewState = VIEW_STATE_UNKNOWN;
                break;
        }
        a.recycle();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mContentView == null) throw new IllegalArgumentException("Content view is not defined");
        setView(VIEW_STATE_UNKNOWN);
    }

    /* All of the addView methods have been overridden so that it can obtain the content view via XML
     It is NOT recommended to add views into MultiStateLayout via the addView methods, but rather use
     any of the setViewForState methods to set views for their given ViewState accordingly */
    @Override
    public void addView(View child) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, index);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, index, params);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, params);
    }

    @Override
    public void addView(View child, int width, int height) {
        if (isValidContentView(child)) mContentView = child;
        super.addView(child, width, height);
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params) {
        if (isValidContentView(child)) mContentView = child;
        return super.addViewInLayout(child, index, params);
    }

    @Override
    protected boolean addViewInLayout(View child, int index, ViewGroup.LayoutParams params, boolean preventRequestLayout) {
        if (isValidContentView(child)) mContentView = child;
        return super.addViewInLayout(child, index, params, preventRequestLayout);
    }


    @Nullable
    public View getView(@ViewState int state) {
        switch (state) {
            case VIEW_STATE_LOADING:
                ensureLoadingView();
                return mLoadingView;
            case VIEW_STATE_CONTENT:
                return mContentView;
            case VIEW_STATE_EMPTY:
                ensureEmptyView();
                return mEmptyView;
            case VIEW_STATE_ERROR:
                ensureErrorView();
                return mErrorView;
            case VIEW_STATE_OTHER:
                ensureOtherView();
                return mErrorView;
            default:
                return null;
        }
    }

    @ViewState
    public int getViewState() {
        return mViewState;
    }


    public void setViewState(@ViewState int state) {
        if (state != mViewState) {
            int previous = mViewState;
            mViewState = state;
            setView(previous);
            if (mListener != null) mListener.onStateChanged(mViewState);
        }
    }


    private void setView(@ViewState int previousState) {
        switch (mViewState) {
            case VIEW_STATE_LOADING:
                ensureLoadingView();
                if (mLoadingView == null) {
                    throw new NullPointerException("Loading View");
                }
                if (mContentView != null) mContentView.setVisibility(View.GONE);
                if (mErrorView != null) mErrorView.setVisibility(View.GONE);
                if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
                if (mOtherView != null) mOtherView.setVisibility(View.GONE);
                mLoadingView.setVisibility(View.VISIBLE);
                break;
            case VIEW_STATE_EMPTY:
                ensureEmptyView();
                if (mEmptyView == null) {
                    throw new NullPointerException("Empty View");
                }
                if (mLoadingView != null) mLoadingView.setVisibility(View.GONE);
                if (mErrorView != null) mErrorView.setVisibility(View.GONE);
                if (mContentView != null) mContentView.setVisibility(View.GONE);
                if (mOtherView != null) mOtherView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
                break;
            case VIEW_STATE_ERROR:
                ensureErrorView();
                if (mErrorView == null) {
                    throw new NullPointerException("Error View");
                }
                if (mLoadingView != null) mLoadingView.setVisibility(View.GONE);
                if (mContentView != null) mContentView.setVisibility(View.GONE);
                if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
                if (mOtherView != null) mOtherView.setVisibility(View.GONE);
                mErrorView.setVisibility(View.VISIBLE);
                break;
            case VIEW_STATE_OTHER:
                ensureOtherView();
                if (mOtherView == null) {
                    throw new NullPointerException("Error View");
                }
                if (mLoadingView != null) mLoadingView.setVisibility(View.GONE);
                if (mContentView != null) mContentView.setVisibility(View.GONE);
                if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
                if (mErrorView != null) mErrorView.setVisibility(View.GONE);
                mOtherView.setVisibility(View.VISIBLE);
                break;
            case VIEW_STATE_CONTENT:
            default:
                if (mContentView == null) {
                    // Should never happen, the view should throw an exception if no content view is present upon creation
                    throw new NullPointerException("Content View");
                }
                if (mLoadingView != null) mLoadingView.setVisibility(View.GONE);
                if (mErrorView != null) mErrorView.setVisibility(View.GONE);
                if (mEmptyView != null) mEmptyView.setVisibility(View.GONE);
                if (mErrorView != null) mErrorView.setVisibility(View.GONE);
                mContentView.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Checks if the given {@link View} is valid for the Content View
     *
     * @param view The {@link View} to check
     * @return
     */
    private boolean isValidContentView(View view) {
        if (mContentView != null && mContentView != view) {
            return false;
        }
        Object tag = view.getTag(R.id.tag_msl);
        if (tag == null) {
            return true;
        }
        if (tag instanceof String) {
            String viewTag = (String) tag;
            if (TextUtils.equals(viewTag, TAG_EMPTY)
                    || TextUtils.equals(viewTag, TAG_ERROR)
                    || TextUtils.equals(viewTag, TAG_LOADING)
                    ) {
                return false;
            }
        }
        return true;
    }


    public void setViewForState(View view, @ViewState int state, boolean switchToState) {
        switch (state) {
            case VIEW_STATE_LOADING:
                if (mLoadingView != null) removeView(mLoadingView);
                mLoadingView = view;
                mLoadingView.setTag(R.id.tag_msl, TAG_LOADING);
                addView(mLoadingView);
                break;

            case VIEW_STATE_EMPTY:
                if (mEmptyView != null) removeView(mEmptyView);
                mEmptyView = view;
                mEmptyView.setTag(R.id.tag_msl, TAG_EMPTY);
                addView(mEmptyView);
                break;

            case VIEW_STATE_ERROR:
                if (mErrorView != null) removeView(mErrorView);
                mErrorView = view;
                mErrorView.setTag(R.id.tag_msl, TAG_ERROR);
                addView(mErrorView);
                break;
            case VIEW_STATE_OTHER:
                if (mOtherView != null) removeView(mOtherView);
                mOtherView = view;
                mOtherView.setTag(R.id.tag_msl, TAG_OTHER);
                addView(mOtherView);
                break;
            case VIEW_STATE_CONTENT:
                if (mContentView != null) removeView(mContentView);
                mContentView = view;
                addView(mContentView);
                break;
        }

        setView(VIEW_STATE_UNKNOWN);
        if (switchToState) setViewState(state);
    }


    public void setViewForState(View view, @ViewState int state) {
        setViewForState(view, state, false);
    }


    public void setViewForState(@LayoutRes int layoutRes, @ViewState int state, boolean switchToState) {
        if (mInflater == null) mInflater = LayoutInflater.from(getContext());
        View view = mInflater.inflate(layoutRes, this, false);
        setViewForState(view, state, switchToState);
    }


    public void setViewForState(@LayoutRes int layoutRes, @ViewState int state) {
        setViewForState(layoutRes, state, false);
    }

    public void setStateListener(StateListener listener) {
        mListener = listener;
    }


    private void ensureLoadingView() {
        if (mLoadingView == null && mLoadingViewResId > -1) {
            mLoadingView = mInflater.inflate(mLoadingViewResId, this, false);
            mLoadingView.setTag(R.id.tag_msl, TAG_LOADING);
            addView(mLoadingView, mLoadingView.getLayoutParams());
            if (mListener != null) mListener.onStateInflated(VIEW_STATE_LOADING, mLoadingView);

            if (mViewState != VIEW_STATE_LOADING) {
                mLoadingView.setVisibility(GONE);
            }
        }
    }

    private void ensureEmptyView() {
        if (mEmptyView == null && mEmptyViewResId > -1) {
            mEmptyView = mInflater.inflate(mEmptyViewResId, this, false);
            mEmptyView.setTag(R.id.tag_msl, TAG_EMPTY);
            addView(mEmptyView, mEmptyView.getLayoutParams());
            if (mListener != null) mListener.onStateInflated(VIEW_STATE_EMPTY, mEmptyView);

            if (mViewState != VIEW_STATE_EMPTY) {
                mEmptyView.setVisibility(GONE);
            }
        }
    }

    private void ensureErrorView() {
        if (mErrorView == null && mErrorViewResId > -1) {
            mErrorView = mInflater.inflate(mErrorViewResId, this, false);
            mErrorView.setTag(R.id.tag_msl, TAG_ERROR);
            addView(mErrorView, mErrorView.getLayoutParams());
            if (mListener != null) mListener.onStateInflated(VIEW_STATE_ERROR, mErrorView);

            if (mViewState != VIEW_STATE_ERROR) {
                mErrorView.setVisibility(GONE);
            }
        }
    }

    private void ensureOtherView() {
        if (mOtherView == null && mOtherViewResId > -1) {
            mOtherView = mInflater.inflate(mOtherViewResId, this, false);
            mOtherView.setTag(R.id.tag_msl, TAG_OTHER);
            addView(mOtherView, mOtherView.getLayoutParams());
            if (mListener != null) mListener.onStateInflated(VIEW_STATE_OTHER, mOtherView);

            if (mViewState != VIEW_STATE_OTHER) {
                mOtherView.setVisibility(GONE);
            }
        }
    }

    public interface StateListener {
        /**
         * Callback for when the {@link ViewState} has changed
         *
         * @param viewState The {@link ViewState} that was switched to
         */
        void onStateChanged(@ViewState int viewState);

        /**
         * Callback for when a {@link ViewState} has been inflated
         *
         * @param viewState The {@link ViewState} that was inflated
         * @param view      The {@link View} that was inflated
         */
        void onStateInflated(@ViewState int viewState, @NonNull View view);
    }
}
