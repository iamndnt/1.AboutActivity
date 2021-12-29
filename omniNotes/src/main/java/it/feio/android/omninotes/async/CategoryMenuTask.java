/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.feio.android.omninotes.async;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import androidx.fragment.app.Fragment;
import de.greenrobot.event.EventBus;
import it.feio.android.omninotes.DetailFragment;
import it.feio.android.omninotes.ListFragment;
import it.feio.android.omninotes.NavigationDrawerFragment;
import it.feio.android.omninotes.R;
import it.feio.android.omninotes.SketchFragment;
import it.feio.android.omninotes.async.bus.NavigationUpdatedEvent;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.models.adapters.CategoryBaseAdapter;
import it.feio.android.omninotes.models.views.NonScrollableListView;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
/*
 * Copyright (C) 2013-2020 Federico Iosue (federico@iosue.it)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_NOTIFICATION_CLICK;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_RESTART_APP;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_SEND_AND_EXIT;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_SHORTCUT;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_SHORTCUT_WIDGET;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_START_APP;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_WIDGET;
import static it.feio.android.omninotes.utils.ConstantsBase.ACTION_WIDGET_TAKE_PHOTO;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_GOOGLE_NOW;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_KEY;
import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_NOTE;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_PASSWORD;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.pixplicity.easyprefs.library.Prefs;
import de.greenrobot.event.EventBus;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import it.feio.android.omninotes.async.UpdateWidgetsTask;
import it.feio.android.omninotes.async.bus.PasswordRemovedEvent;
import it.feio.android.omninotes.async.bus.SwitchFragmentEvent;
import it.feio.android.omninotes.async.notes.NoteProcessorDelete;
import it.feio.android.omninotes.databinding.ActivityMainBinding;
import it.feio.android.omninotes.db.DbHelper;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.helpers.NotesHelper;
import it.feio.android.omninotes.intro.IntroActivity;
import it.feio.android.omninotes.models.Attachment;
import it.feio.android.omninotes.models.Category;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.ONStyle;
import it.feio.android.omninotes.utils.FileProviderHelper;
import it.feio.android.omninotes.utils.PasswordHelper;
import it.feio.android.omninotes.utils.SystemHelper;
import it.feio.android.pixlui.links.UrlCompleter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import lombok.Getter;
import lombok.Setter;

import static it.feio.android.omninotes.utils.ConstantsBase.INTENT_UPDATE_DASHCLOCK;
import static it.feio.android.omninotes.utils.ConstantsBase.PREF_NAVIGATION;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.ViewConfiguration;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.pixplicity.easyprefs.library.Prefs;
import it.feio.android.omninotes.helpers.LanguageHelper;
import it.feio.android.omninotes.helpers.LogDelegate;
import it.feio.android.omninotes.models.Note;
import it.feio.android.omninotes.models.PasswordValidator;
import it.feio.android.omninotes.utils.Navigation;
import it.feio.android.omninotes.utils.PasswordHelper;
import it.feio.android.omninotes.widget.ListWidgetProvider;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

@SuppressLint("Registered")
class BaseActivity extends AppCompatActivity {

  protected static final int TRANSITION_VERTICAL = 0;
  protected static final int TRANSITION_HORIZONTAL = 1;

  protected String navigation;
  protected String navigationTmp; // used for widget navigation


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.menu_list, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  protected void attachBaseContext(Context newBase) {
    Context context = LanguageHelper.updateLanguage(newBase, null);
    super.attachBaseContext(context);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    // Forces menu overflow icon
    try {
      ViewConfiguration config = ViewConfiguration.get(this.getApplicationContext());
      Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
      if (menuKeyField != null) {
        menuKeyField.setAccessible(true);
        menuKeyField.setBoolean(config, false);
      }
    } catch (Exception e) {
      LogDelegate.w("Just a little issue in physical menu button management", e);
    }
    super.onCreate(savedInstanceState);
  }


  @Override
  protected void onResume() {
    super.onResume();
    String navNotes = getResources().getStringArray(R.array.navigation_list_codes)[0];
    navigation = Prefs.getString(PREF_NAVIGATION, navNotes);
    LogDelegate.d(Prefs.getAll().toString());
  }


  protected void showToast(CharSequence text, int duration) {
    if (Prefs.getBoolean("settings_enable_info", true)) {
      Toast.makeText(getApplicationContext(), text, duration).show();
    }
  }


  /**
   * Method to validate security password to protect a list of notes. When "Request password on
   * access" in switched on this check not required all the times. It uses an interface callback.
   */
  public void requestPassword(final Activity mActivity, List<Note> notes,
                              final PasswordValidator mPasswordValidator) {
    if (Prefs.getBoolean("settings_password_access", false)) {
      mPasswordValidator.onPasswordValidated(PasswordValidator.Result.SUCCEED);
      return;
    }

    boolean askForPassword = false;
    for (Note note : notes) {
      if (note.isLocked()) {
        askForPassword = true;
        break;
      }
    }
    if (askForPassword) {
      PasswordHelper.requestPassword(mActivity, mPasswordValidator);
    } else {
      mPasswordValidator.onPasswordValidated(PasswordValidator.Result.SUCCEED);
    }
  }


  public boolean updateNavigation(String nav) {
    if (nav.equals(navigationTmp) || (navigationTmp == null && Navigation.getNavigationText()
            .equals(nav))) {
      return false;
    }
    Prefs.edit().putString(PREF_NAVIGATION, nav).apply();
    navigation = nav;
    navigationTmp = null;
    return true;
  }


  /**
   * Retrieves resource by name
   */
  private String getStringResourceByName(String aString) {
    String packageName = getApplicationContext().getPackageName();
    int resId = getResources().getIdentifier(aString, "string", packageName);
    return getString(resId);
  }


  /**
   * Notifies App Widgets about data changes so they can update theirselves
   */
  public static void notifyAppWidgets(Context context) {
    // Home widgets
    AppWidgetManager mgr = AppWidgetManager.getInstance(context);
    int[] ids = mgr.getAppWidgetIds(new ComponentName(context, ListWidgetProvider.class));
    LogDelegate.d("Notifies AppWidget data changed for widgets " + Arrays.toString(ids));
    mgr.notifyAppWidgetViewDataChanged(ids, R.id.widget_list);

    // Dashclock
    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(INTENT_UPDATE_DASHCLOCK));
  }


  @SuppressLint("InlinedApi")
  protected void animateTransition(FragmentTransaction transaction, int direction) {
    if (direction == TRANSITION_HORIZONTAL) {
      transaction.setCustomAnimations(R.anim.fade_in_support, R.anim.fade_out_support,
              R.anim.fade_in_support, R.anim.fade_out_support);
    }
    if (direction == TRANSITION_VERTICAL) {
      transaction.setCustomAnimations(
              R.anim.anim_in, R.anim.anim_out, R.anim.anim_in_pop, R.anim.anim_out_pop);
    }
  }


  protected void setActionBarTitle(String title) {
    // Creating a spannable to support custom fonts on ActionBar
    int actionBarTitle = Resources.getSystem().getIdentifier("action_bar_title", "ID", "android");
    android.widget.TextView actionBarTitleView = getWindow().findViewById(actionBarTitle);
    Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf");
    if (actionBarTitleView != null) {
      actionBarTitleView.setTypeface(font);
    }

    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(title);
    }
  }


  public String getNavigationTmp() {
    return navigationTmp;
  }


  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    return keyCode == KeyEvent.KEYCODE_MENU || super.onKeyDown(keyCode, event);
  }
}


class MainActivity extends BaseActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

  private boolean isPasswordAccepted = false;
  public final static String FRAGMENT_DRAWER_TAG = "fragment_drawer";
  public final static String FRAGMENT_LIST_TAG = "fragment_list";
  public final static String FRAGMENT_DETAIL_TAG = "fragment_detail";
  public final static String FRAGMENT_SKETCH_TAG = "fragment_sketch";
  @Getter @Setter
  private Uri sketchUri;
  boolean prefsChanged = false;
  private FragmentManager mFragmentManager;

  ActivityMainBinding binding;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTheme(R.style.OmniNotesTheme_ApiSpec);

    binding = ActivityMainBinding.inflate(getLayoutInflater());
    View view = binding.getRoot();
    setContentView(view);

    EventBus.getDefault().register(this);
    Prefs.getPreferences().registerOnSharedPreferenceChangeListener(this);

    initUI();

    if (IntroActivity.mustRun()) {
      startActivity(new Intent(getApplicationContext(), IntroActivity.class));
    }

  }

  @Override
  protected void onResume() {
    super.onResume();
    if (isPasswordAccepted) {
      init();
    } else {
      checkPassword();
    }
  }


  @Override
  protected void onStop() {
    super.onStop();
    EventBus.getDefault().unregister(this);
  }


  private void initUI() {
    setSupportActionBar(binding.toolbar.toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
  }


  /**
   * This method starts the bootstrap chain.
   */
  private void checkPassword() {
    if (Prefs.getString(PREF_PASSWORD, null) != null
            && Prefs.getBoolean("settings_password_access", false)) {
      PasswordHelper.requestPassword(this, passwordConfirmed -> {
        switch (passwordConfirmed) {
          case SUCCEED:
            init();
            break;
          case FAIL:
            finish();
            break;
          case RESTORE:
            PasswordHelper.resetPassword(this);
        }
      });
    } else {
      init();
    }
  }


  public void onEvent(PasswordRemovedEvent passwordRemovedEvent) {
    showMessage(R.string.password_successfully_removed, ONStyle.ALERT);
    init();
  }


  private void init() {
    isPasswordAccepted = true;

    getFragmentManagerInstance();

    NavigationDrawerFragment mNavigationDrawerFragment = (NavigationDrawerFragment) getFragmentManagerInstance()
            .findFragmentById(R.id.navigation_drawer);
    if (mNavigationDrawerFragment == null) {
      FragmentTransaction fragmentTransaction = getFragmentManagerInstance().beginTransaction();
      fragmentTransaction.replace(R.id.navigation_drawer, new NavigationDrawerFragment(),
              FRAGMENT_DRAWER_TAG).commit();
    }

    if (getFragmentManagerInstance().findFragmentByTag(FRAGMENT_LIST_TAG) == null) {
      FragmentTransaction fragmentTransaction = getFragmentManagerInstance().beginTransaction();
      fragmentTransaction.add(R.id.fragment_container, new ListFragment(), FRAGMENT_LIST_TAG)
              .commit();
    }

    handleIntents();
  }

  private FragmentManager getFragmentManagerInstance() {
    if (mFragmentManager == null) {
      mFragmentManager = getSupportFragmentManager();
    }
    return mFragmentManager;
  }

  @Override
  protected void onNewIntent(Intent intent) {
    if (intent.getAction() == null) {
      intent.setAction(ACTION_START_APP);
    }
    super.onNewIntent(intent);
    setIntent(intent);
    handleIntents();
    LogDelegate.d("onNewIntent");
  }


  public MenuItem getSearchMenuItem() {
    Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
    if (f != null) {
      return ((ListFragment) f).getSearchMenuItem();
    } else {
      return null;
    }
  }


  public void editTag(Category tag) {
    Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
    if (f != null) {
      ((ListFragment) f).editCategory(tag);
    }
  }

  public void initNotesList(Intent intent) {
    if (intent != null) {
      Fragment searchTagFragment = startSearchView();
      new Handler(getMainLooper()).post(() -> ((ListFragment) searchTagFragment).initNotesList(intent));
    }
  }

  public Fragment startSearchView() {
    FragmentTransaction transaction = getFragmentManagerInstance().beginTransaction();
    animateTransition(transaction, TRANSITION_HORIZONTAL);
    ListFragment mListFragment = new ListFragment();
    transaction.replace(R.id.fragment_container, mListFragment, FRAGMENT_LIST_TAG).addToBackStack
            (FRAGMENT_DETAIL_TAG).commit();
    Bundle args = new Bundle();
    args.putBoolean("setSearchFocus", true);
    mListFragment.setArguments(args);
    return mListFragment;
  }


  public void commitPending() {
    Fragment f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
    if (f != null) {
      ((ListFragment) f).commitPending();
    }
  }


  /**
   * Checks if allocated fragment is of the required type and then returns it or returns null
   */
  private Fragment checkFragmentInstance(int id, Object instanceClass) {
    Fragment result = null;
    Fragment fragment = getFragmentManagerInstance().findFragmentById(id);
    if (fragment != null && instanceClass.equals(fragment.getClass())) {
      result = fragment;
    }
    return result;
  }

  @Override
  public void onBackPressed() {

    // SketchFragment
    Fragment f = checkFragmentInstance(R.id.fragment_container, SketchFragment.class);
    if (f != null) {
      ((SketchFragment) f).save();

      // Removes forced portrait orientation for this fragment
      setRequestedOrientation(
              ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

      getFragmentManagerInstance().popBackStack();
      return;
    }

    // DetailFragment
    f = checkFragmentInstance(R.id.fragment_container, DetailFragment.class);
    if (f != null) {
      ((DetailFragment) f).goBack = true;
      ((DetailFragment) f).saveAndExit((DetailFragment) f);
      return;
    }

    // ListFragment
    f = checkFragmentInstance(R.id.fragment_container, ListFragment.class);
    if (f != null) {
      // Before exiting from app the navigation drawer is opened
      if (Prefs.getBoolean("settings_navdrawer_on_exit", false) && getDrawerLayout() != null &&
              !getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
        getDrawerLayout().openDrawer(GravityCompat.START);
      } else if (!Prefs.getBoolean("settings_navdrawer_on_exit", false) && getDrawerLayout() != null
              &&
              getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
        getDrawerLayout().closeDrawer(GravityCompat.START);
      } else {
        if (!((ListFragment) f).closeFab()) {
          isPasswordAccepted = false;
          super.onBackPressed();
        }
      }
      return;
    }
    super.onBackPressed();
  }


  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString("navigationTmp", navigationTmp);
  }


  @Override
  protected void onPause() {
    super.onPause();
    Crouton.cancelAllCroutons();
  }


  public DrawerLayout getDrawerLayout() {
    return binding.drawerLayout;
  }


  public ActionBarDrawerToggle getDrawerToggle() {
    if (getFragmentManagerInstance().findFragmentById(R.id.navigation_drawer) != null) {
      return ((NavigationDrawerFragment) getFragmentManagerInstance().findFragmentById(
              R.id.navigation_drawer)).mDrawerToggle;
    } else {
      return null;
    }
  }


  /**
   * Finishes multiselection mode started by ListFragment
   */
  public void finishActionMode() {
    ListFragment fragment = (ListFragment) getFragmentManagerInstance()
            .findFragmentByTag(FRAGMENT_LIST_TAG);
    if (fragment != null) {
      fragment.finishActionMode();
    }
  }


  Toolbar getToolbar() {
    return binding.toolbar.toolbar;
  }


  private void handleIntents() {
    Intent i = getIntent();

    if (i.getAction() == null) {
      return;
    }

    if (ACTION_RESTART_APP.equals(i.getAction())) {
      SystemHelper.restartApp(getApplicationContext(), MainActivity.class);
    }

    if (receivedIntent(i)) {
      Note note = i.getParcelableExtra(INTENT_NOTE);
      if (note == null) {
        note = DbHelper.getInstance().getNote(i.getIntExtra(INTENT_KEY, 0));
      }
      // Checks if the same note is already opened to avoid to open again
      if (note != null && noteAlreadyOpened(note)) {
        return;
      }
      // Empty note instantiation
      if (note == null) {
        note = new Note();
      }
      switchToDetail(note);
      return;
    }

    if (ACTION_SEND_AND_EXIT.equals(i.getAction())) {
      saveAndExit(i);
      return;
    }

    // Tag search
    if (Intent.ACTION_VIEW.equals(i.getAction()) && i.getDataString()
            .startsWith(UrlCompleter.HASHTAG_SCHEME)) {
      switchToList();
      return;
    }

    // Home launcher shortcut widget
    if (Intent.ACTION_VIEW.equals(i.getAction()) && i.getData() != null) {
      Long id = Long.valueOf(Uri.parse(i.getDataString()).getQueryParameter("id"));
      Note note = DbHelper.getInstance().getNote(id);
      if (note == null) {
        showMessage(R.string.note_doesnt_exist, ONStyle.ALERT);
        return;
      }
      switchToDetail(note);
      return;
    }

    // Home launcher "new note" shortcut widget
    if (ACTION_SHORTCUT_WIDGET.equals(i.getAction())) {
      switchToDetail(new Note());
      return;
    }
  }


  /**
   * Used to perform a quick text-only note saving (eg. Tasker+Pushbullet)
   */
  private void saveAndExit(Intent i) {
    Note note = new Note();
    note.setTitle(i.getStringExtra(Intent.EXTRA_SUBJECT));
    note.setContent(i.getStringExtra(Intent.EXTRA_TEXT));
    DbHelper.getInstance().updateNote(note, true);
    showToast(getString(R.string.note_updated), Toast.LENGTH_SHORT);
    finish();
  }


  private boolean receivedIntent(Intent i) {
    return ACTION_SHORTCUT.equals(i.getAction())
            || ACTION_NOTIFICATION_CLICK.equals(i.getAction())
            || ACTION_WIDGET.equals(i.getAction())
            || ACTION_WIDGET_TAKE_PHOTO.equals(i.getAction())
            || ((Intent.ACTION_SEND.equals(i.getAction())
            || Intent.ACTION_SEND_MULTIPLE.equals(i.getAction())
            || INTENT_GOOGLE_NOW.equals(i.getAction()))
            && i.getType() != null)
            || i.getAction().contains(ACTION_NOTIFICATION_CLICK);
  }


  private boolean noteAlreadyOpened(Note note) {
    DetailFragment detailFragment = (DetailFragment) getFragmentManagerInstance().findFragmentByTag(
            FRAGMENT_DETAIL_TAG);
    return detailFragment != null && NotesHelper.haveSameId(note, detailFragment.getCurrentNote());
  }


  public void switchToList() {
    FragmentTransaction transaction = getFragmentManagerInstance().beginTransaction();
    animateTransition(transaction, TRANSITION_HORIZONTAL);
    ListFragment mListFragment = new ListFragment();
    transaction.replace(R.id.fragment_container, mListFragment, FRAGMENT_LIST_TAG).addToBackStack
            (FRAGMENT_DETAIL_TAG).commitAllowingStateLoss();
    if (getDrawerToggle() != null) {
      getDrawerToggle().setDrawerIndicatorEnabled(false);
    }
    getFragmentManagerInstance().getFragments();
    EventBus.getDefault().post(new SwitchFragmentEvent(SwitchFragmentEvent.Direction.PARENT));
  }


  public void switchToDetail(Note note) {
    FragmentTransaction transaction = getFragmentManagerInstance().beginTransaction();
    animateTransition(transaction, TRANSITION_HORIZONTAL);
    DetailFragment mDetailFragment = new DetailFragment();
    Bundle b = new Bundle();
    b.putParcelable(INTENT_NOTE, note);
    mDetailFragment.setArguments(b);
    if (getFragmentManagerInstance().findFragmentByTag(FRAGMENT_DETAIL_TAG) == null) {
      transaction.replace(R.id.fragment_container, mDetailFragment, FRAGMENT_DETAIL_TAG)
              .addToBackStack(FRAGMENT_LIST_TAG)
              .commitAllowingStateLoss();
    } else {
      getFragmentManagerInstance().popBackStackImmediate();
      transaction.replace(R.id.fragment_container, mDetailFragment, FRAGMENT_DETAIL_TAG)
              .addToBackStack(FRAGMENT_DETAIL_TAG)
              .commitAllowingStateLoss();
    }
  }


  /**
   * Notes sharing
   */
  public void shareNote(Note note) {

    String titleText = note.getTitle();

    String contentText = titleText
            + System.getProperty("line.separator")
            + note.getContent();

    Intent shareIntent = new Intent();
    // Prepare sharing intent with only text
    if (note.getAttachmentsList().isEmpty()) {
      shareIntent.setAction(Intent.ACTION_SEND);
      shareIntent.setType("text/plain");

      // Intent with single image attachment
    } else if (note.getAttachmentsList().size() == 1) {
      shareIntent.setAction(Intent.ACTION_SEND);
      Attachment attachment = note.getAttachmentsList().get(0);
      shareIntent.setType(attachment.getMime_type());
      shareIntent.putExtra(Intent.EXTRA_STREAM, FileProviderHelper.getShareableUri(attachment));

      // Intent with multiple images
    } else if (note.getAttachmentsList().size() > 1) {
      shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
      ArrayList<Uri> uris = new ArrayList<>();
      // A check to decide the mime type of attachments to share is done here
      HashMap<String, Boolean> mimeTypes = new HashMap<>();
      for (Attachment attachment : note.getAttachmentsList()) {
        uris.add(FileProviderHelper.getShareableUri(attachment));
        mimeTypes.put(attachment.getMime_type(), true);
      }
      // If many mime types are present a general type is assigned to intent
      if (mimeTypes.size() > 1) {
        shareIntent.setType("*/*");
      } else {
        shareIntent.setType((String) mimeTypes.keySet().toArray()[0]);
      }

      shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
    }
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, titleText);
    shareIntent.putExtra(Intent.EXTRA_TEXT, contentText);

    startActivity(Intent
            .createChooser(shareIntent, getResources().getString(R.string.share_message_chooser)));
  }


  /**
   * Single note permanent deletion
   *
   * @param note Note to be deleted
   */
  public void deleteNote(Note note) {
    new NoteProcessorDelete(Collections.singletonList(note)).process();
    BaseActivity.notifyAppWidgets(this);
    LogDelegate.d("Deleted permanently note with ID '" + note.get_id() + "'");
  }


  public void updateWidgets() {
    new UpdateWidgetsTask(getApplicationContext())
            .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
  }


  public void showMessage(int messageId, Style style) {
    showMessage(getString(messageId), style);
  }


  public void showMessage(String message, Style style) {
    // ViewGroup used to show Crouton keeping compatibility with the new Toolbar
    runOnUiThread(
            () -> Crouton.makeText(this, message, style, binding.croutonHandle.croutonHandle).show());
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    prefsChanged = true;
  }

}



public class CategoryMenuTask extends AsyncTask<Void, Void, List<Category>> {

  private final WeakReference<Fragment> mFragmentWeakReference;
  private final MainActivity mainActivity;
  private NonScrollableListView mDrawerCategoriesList;
  private View settingsView;
  private View settingsViewCat;
  private NonScrollableListView mDrawerList;


  public CategoryMenuTask(Fragment mFragment) {
    mFragmentWeakReference = new WeakReference<>(mFragment);
    this.mainActivity = (MainActivity) mFragment.getActivity();
  }


  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    mDrawerList = mainActivity.findViewById(R.id.drawer_nav_list);
    LayoutInflater inflater = (LayoutInflater) mainActivity
        .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

    settingsView = mainActivity.findViewById(R.id.settings_view);

    // Settings view when categories are available
    mDrawerCategoriesList = mainActivity.findViewById(R.id.drawer_tag_list);
    if (mDrawerCategoriesList.getAdapter() == null
        && mDrawerCategoriesList.getFooterViewsCount() == 0) {
      settingsViewCat = inflater.inflate(R.layout.drawer_category_list_footer, null);
      mDrawerCategoriesList.addFooterView(settingsViewCat);
    } else {
      settingsViewCat = mDrawerCategoriesList.getChildAt(mDrawerCategoriesList.getChildCount() - 1);
    }

  }


  @Override
  protected List<Category> doInBackground(Void... params) {
    if (isAlive()) {
      return buildCategoryMenu();
    } else {
      cancel(true);
      return Collections.emptyList();
    }
  }


  @Override
  protected void onPostExecute(final List<Category> categories) {
    if (isAlive()) {
      mDrawerCategoriesList.setAdapter(new CategoryBaseAdapter(mainActivity, categories,
          mainActivity.getNavigationTmp()));
      if (categories.isEmpty()) {
        setWidgetVisibility(settingsViewCat, false);
        setWidgetVisibility(settingsView, true);
      } else {
        setWidgetVisibility(settingsViewCat, true);
        setWidgetVisibility(settingsView, false);
      }
      mDrawerCategoriesList.justifyListViewHeightBasedOnChildren();
    }
  }


  private void setWidgetVisibility(View view, boolean visible) {
    if (view != null) {
      view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
  }


  private boolean isAlive() {
    return mFragmentWeakReference.get() != null
        && mFragmentWeakReference.get().isAdded()
        && mFragmentWeakReference.get().getActivity() != null
        && !mFragmentWeakReference.get().getActivity().isFinishing();
  }


  private List<Category> buildCategoryMenu() {

    List<Category> categories = DbHelper.getInstance().getCategories();

    View settings = categories.isEmpty() ? settingsView : settingsViewCat;
    if (settings == null) {
      return categories;
    }

    mainActivity.runOnUiThread(() -> {
      settings.setOnClickListener(v -> {

      });

      buildCategoryMenuClickEvent();

      buildCategoryMenuLongClickEvent();

    });

    return categories;
  }

  private void buildCategoryMenuLongClickEvent() {
    mDrawerCategoriesList.setOnItemLongClickListener((arg0, view, position, arg3) -> {
      if (mDrawerCategoriesList.getAdapter() != null) {
        Object item = mDrawerCategoriesList.getAdapter().getItem(position);
        // Ensuring that clicked item is not the ListView header
        if (item != null) {
          mainActivity.editTag((Category) item);
        }
      } else {
        mainActivity.showMessage(R.string.category_deleted, ONStyle.ALERT);
      }
      return true;
    });
  }

  private void buildCategoryMenuClickEvent() {
    mDrawerCategoriesList.setOnItemClickListener((arg0, arg1, position, arg3) -> {

      Object item = mDrawerCategoriesList.getAdapter().getItem(position);
      if (mainActivity.updateNavigation(String.valueOf(((Category) item).getId()))) {
        mDrawerCategoriesList.setItemChecked(position, true);
        // Forces redraw
        if (mDrawerList != null) {
          mDrawerList.setItemChecked(0, false);
          EventBus.getDefault()
              .post(new NavigationUpdatedEvent(mDrawerCategoriesList.getItemAtPosition
                  (position)));
        }
      }
    });
  }

}
