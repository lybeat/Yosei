package cc.arturia.yosei.module.settings

import android.widget.Toast
import cc.arturia.yosei.R
import cc.arturia.yosei.data.Folder
import cc.arturia.yosei.data.SpConfig
import cc.arturia.yosei.event.FolderHideEvent
import cc.arturia.yosei.event.RxBus
import cc.arturia.yosei.module.base.BaseFragment
import com.afollestad.materialdialogs.MaterialDialog
import io.realm.Realm
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlin.properties.Delegates

/**
 * Author: Arturia
 * Date: 2018/11/12
 */
class SettingsFragment : BaseFragment() {

    private var realm: Realm by Delegates.notNull()

    override fun getLayoutResId(): Int = R.layout.fragment_settings

    override fun initData() {
        realm = Realm.getDefaultInstance()
    }

    override fun initView() {
        menu_file_hide.isChecked = SpConfig.getFileHide(activity!!)
    }

    override fun bindListener() {
        menu_file_hide.setOnClickListener { _ ->
            showPsdDialog()
        }
        menu_play_action.setOnClickListener { }
        menu_render_view.setOnClickListener { }
        menu_about.setOnClickListener { }
        menu_license.setOnClickListener { }
    }

    private fun showPsdDialog() {
        val hint: String = if (SpConfig.getLockPassword(activity!!).isNotEmpty()) {
            getString(R.string.input_password)
        } else {
            getString(R.string.setting_password)
        }
        MaterialDialog.Builder(activity!!)
                .input(hint, "", false
                ) { _, input ->
                    when {
                        SpConfig.getLockPassword(activity!!).isEmpty() -> {
                            SpConfig.setLockPassword(activity!!, input.toString())
                            toggleFileHide()
                        }
                        input.toString() == SpConfig.getLockPassword(activity!!) -> toggleFileHide()
                        else -> {
                            menu_file_hide.isChecked = !menu_file_hide.isChecked
                            Toast.makeText(activity!!, R.string.password_error, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .onPositive { dialog, _ ->
                    dialog.dismiss()
                }
                .cancelListener {
                    menu_file_hide.isChecked = !menu_file_hide.isChecked
                }
                .show()
    }

    private fun toggleFileHide() {
        menu_file_hide.isChecked = !SpConfig.getFileHide(activity!!)
        SpConfig.setFileHide(activity!!, menu_file_hide.isChecked)
        if (menu_file_hide.isChecked) {
            insertHideFolder()
        } else {
            deleteHideFolder()
        }
        RxBus.instance.post(FolderHideEvent())
    }

    private fun insertHideFolder() {
        val folder = Folder()
        folder.name = getString(R.string.app_name)
        folder.path = ""
        folder.cover = ""
        folder.timestamp = 0L
        realm.executeTransaction { realm.copyToRealmOrUpdate(folder) }
    }

    private fun deleteHideFolder() {
        val folder = realm.where(Folder::class.java)
                .equalTo("name", getString(R.string.app_name))
                .findFirst()
        realm.executeTransaction { folder!!.deleteFromRealm() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        realm.close()
    }
}