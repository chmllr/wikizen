(ns wikizen.ui
  (:require-macros [hiccups.core :as hiccups])
  (:require [hiccups.runtime :as hiccupsrt]
            [goog.dom :as dom]))


(hiccups/defhtml
  wiki-page []
  [:div "Here comes the Zen..."])
