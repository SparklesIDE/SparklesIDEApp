package com.rk.xededitor.MainActivity

import android.graphics.Color
import android.net.Uri
import android.view.View
import android.view.WindowManager
import androidx.appcompat.widget.PopupMenu
import androidx.documentfile.provider.DocumentFile
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.rk.xededitor.Decompress
import com.rk.xededitor.MainActivity.treeview2.MA
import com.rk.xededitor.R
import com.rk.xededitor.Settings.SettingsData
import com.rk.xededitor.plugin.PluginServer
import java.io.File

class Init(activity: MainActivity) {
  init {
    Thread {
      
      val file = File(activity.filesDir,"crash-journal.log")
      if (file.exists()){
        file.delete()
      }
      
      with(activity) {
        Data.fileList = ArrayList()
        PluginServer(application).start()
        
        if (!SettingsData.isDarkMode(this)) {
          //light mode
          window.navigationBarColor = Color.parseColor("#FEF7FF")
          val decorView = window.decorView
          var flags = decorView.systemUiVisibility
          flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
          decorView.systemUiVisibility = flags
        }
        if (SettingsData.isDarkMode(this) && SettingsData.isOled(this)) {
          binding.drawerLayout.setBackgroundColor(Color.BLACK)
          binding.navView.setBackgroundColor(Color.BLACK)
          binding.main.setBackgroundColor(Color.BLACK)
          binding.appbar.setBackgroundColor(Color.BLACK)
          binding.toolbar.setBackgroundColor(Color.BLACK)
          binding.tabs.setBackgroundColor(Color.BLACK)
          binding.mainView.setBackgroundColor(Color.BLACK)
          val window = window
          window.navigationBarColor = Color.BLACK
          window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
          window.statusBarColor = Color.BLACK
        }
        Data.mTabLayout.setOnTabSelectedListener(object : OnTabSelectedListener {
          override fun onTabSelected(tab: TabLayout.Tab) {
            viewPager.setCurrentItem(tab.position)
            Data.fragments[Data.mTabLayout.selectedTabPosition].updateUndoRedo()
          }
          
          override fun onTabUnselected(tab: TabLayout.Tab) {}
          override fun onTabReselected(tab: TabLayout.Tab) {
            val popupMenu = PopupMenu(activity, tab.view)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.tab_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
              val id = item.itemId
              if (id == R.id.close_this) {
                adapter.removeFragment(Data.mTabLayout.selectedTabPosition)
              } else if (id == R.id.close_others) {
                adapter.closeOthers(viewPager.currentItem)
              } else if (id == R.id.close_all) {
                adapter.clear()
              }
              for (i in 0 until Data.mTabLayout.tabCount) {
                val tab = Data.mTabLayout.getTabAt(i)
                if (tab != null) {
                  val name = Data.titles[i]
                  if (name != null) {
                    tab.setText(name)
                  }
                }
              }
              if (Data.mTabLayout.tabCount < 1) {
                binding.tabs.visibility = View.GONE
                binding.mainView.visibility = View.GONE
                binding.openBtn.visibility = View.VISIBLE
              }
              val visible =
                !(Data.fragments == null || Data.fragments.isEmpty())
              Data.menu.findItem(R.id.search).setVisible(visible)
              Data.menu.findItem(R.id.action_save).setVisible(visible)
              Data.menu.findItem(R.id.action_print).setVisible(visible)
              Data.menu.findItem(R.id.action_all).setVisible(visible)
              Data.menu.findItem(R.id.batchrep).setVisible(visible)
              Data.menu.findItem(R.id.share).setVisible(visible)
              true
            }
            popupMenu.show()
          }
        })
        
        //todo use shared prefs instead of files
        if (!File(getExternalFilesDir(null).toString() + "/unzip").exists()) {
          Thread {
            try {
              Decompress.unzipFromAssets(
                this, "files.zip", getExternalFilesDir(null).toString() + "/unzip"
              )
              File(getExternalFilesDir(null).toString() + "files").delete()
              File(getExternalFilesDir(null).toString() + "files.zip").delete()
              File(getExternalFilesDir(null).toString() + "textmate").delete()
            } catch (e: Exception) {
              e.printStackTrace()
            }
          }.start()
        }
        
        val uriString = SettingsData.getSetting(this, "lastOpenedUri", "null")
        if (uriString != "null") {
          val uri = Uri.parse(uriString)
          if (hasUriPermission(uri)) {
            Data.rootFolder = DocumentFile.fromTreeUri(this, uri)
            //binding.tabs.setVisibility(View.VISIBLE);
            binding.mainView.visibility = View.VISIBLE
            binding.safbuttons.visibility = View.GONE
            binding.maindrawer.visibility = View.VISIBLE
            binding.drawerToolbar.visibility = View.VISIBLE
            
            runOnUiThread { MA(this, Data.rootFolder) }
            
            var name = Data.rootFolder.name!!
            if (name.length > 18) {
              name = Data.rootFolder.name!!.substring(0, 15) + "..."
            }
            binding.rootDirLabel.text = name
          }
        }
        
      }
      
      
    }.start()
  }
}