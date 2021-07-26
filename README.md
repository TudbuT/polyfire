PolyFire
========

ClickGUI opens with NUMROW_0


How to use:
===========

- Run `-jsmodules` to initialize
- Navigate to your .minecraft folder
- Go to `config/pf/modules/`
- Copy Example.pfmodule.js and paste it, then rename the copy to `YOUR_MODULE_ID_HERE.pfmodule.js` 
  (if the .js was invisible before renaming, do not add it!) - REMEMBER TO REMOVE THE `(1)` FROM THE END! 
  THE NAME HAS TO FIT!
- You can now load the module with `-jsmodules add YOUR_MODULE_ID_HERE`. The module will then load
  whenever the client loads.


You can unload and reload modules too: `-jsmodules remove YOUR_MODULE_ID_HERE` and 
`-jsmodules reload YOUR_MODULE_ID_HERE`. To reload all modules, run `-jsmodules reload`

For modules to be able to run, the parent module has to be enabled, so remember to enable the JSModules module
in the ClickGUI!

Documentation:
==============

Soon™