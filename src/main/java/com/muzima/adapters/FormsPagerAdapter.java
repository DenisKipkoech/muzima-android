package com.muzima.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.muzima.MuzimaApplication;
import com.muzima.controller.FormController;
import com.muzima.listeners.DownloadListener;
import com.muzima.view.forms.CompleteFormsListFragment;
import com.muzima.view.forms.DownloadedFormsListFragment;
import com.muzima.view.forms.FormsListFragment;
import com.muzima.view.forms.IncompleteFormsListFragment;
import com.muzima.view.forms.NewFormsListFragment;
import com.muzima.view.forms.SyncedFormsListFragment;

import static com.muzima.adapters.TagsListAdapter.TagsChangedListener;

public class FormsPagerAdapter extends FragmentPagerAdapter implements DownloadListener<Integer[]>, TagsChangedListener{
    private static final int TAB_All = 0;
    private static final int TAB_DOWNLOADED = 1;
    private static final int TAB_COMPLETE = 2;
    private static final int TAB_INCOMPLETE = 3;
    private static final int TAB_SYNCED = 4;

    private PagerView[] pagers;

    public FormsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        initPagerViews(context);
    }

    @Override
    public Fragment getItem(int position) {
        return pagers[position].fragment;
    }

    @Override
    public int getCount() {
        return pagers.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return pagers[position].title;
    }

    @Override
    public void downloadTaskComplete(Integer[] result) {
        pagers[TAB_All].fragment.formDownloadComplete(result);
    }

    @Override
    public void onTagsChanged() {
        pagers[TAB_All].fragment.tagsChanged();
    }

    private void initPagerViews(Context context) {
        final Resources resources = context.getResources();
        pagers = new PagerView[5];
        FormController formController = ((MuzimaApplication) context.getApplicationContext()).getFormController();

        NewFormsListFragment newFormsListFragment = NewFormsListFragment.newInstance(formController);
        DownloadedFormsListFragment downloadedFormsListFragment = DownloadedFormsListFragment.newInstance(formController);
        CompleteFormsListFragment completeFormsListFragment = CompleteFormsListFragment.newInstance(formController);
        IncompleteFormsListFragment incompleteFormsListFragment = IncompleteFormsListFragment.newInstance(formController);
        SyncedFormsListFragment syncedFormsListFragment = SyncedFormsListFragment.newInstance(formController);

        newFormsListFragment.setTemplateDownloadCompleteListener(downloadedFormsListFragment);

        pagers[TAB_All] = new PagerView("All", newFormsListFragment);
        pagers[TAB_DOWNLOADED] = new PagerView("Downloaded", downloadedFormsListFragment);
        pagers[TAB_COMPLETE] = new PagerView("Complete", completeFormsListFragment);
        pagers[TAB_INCOMPLETE] = new PagerView("Incomplete", incompleteFormsListFragment);
        pagers[TAB_SYNCED] = new PagerView("Synced", syncedFormsListFragment);
    }

    private static class PagerView {
        String title;
        FormsListFragment fragment;

        private PagerView(String title, FormsListFragment fragment) {
            this.title = title;
            this.fragment = fragment;
        }
    }
}
