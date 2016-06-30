(ns uxbox.main.ui.lightbox
  (:require [sablono.core :as html :refer-macros [html]]
            [rum.core :as rum]
            [lentes.core :as l]
            [uxbox.main.state :as st]
            [uxbox.main.data.lightbox :as udl]
            [uxbox.common.ui.mixins :as mx]
            [uxbox.main.ui.keyboard :as k]
            [uxbox.util.dom :as dom]
            [uxbox.util.data :refer (classnames)]
            [goog.events :as events])
  (:import goog.events.EventType))

;; --- Lentes

(def ^:const ^:private lightbox-l
  (-> (l/key :lightbox)
      (l/derive st/state)))

;; --- Lightbox (Component)

(defmulti render-lightbox :name)
(defmethod render-lightbox :default [_] nil)

(defn- on-esc-clicked
  [event]
  (when (k/esc? event)
    (udl/close!)
    (dom/stop-propagation event)))

(defn- on-out-clicked
  [own event]
  (let [parent (mx/get-ref-dom own "parent")
        current (dom/get-target event)]
    (when (dom/equals? parent current)
      (udl/close!))))

(defn- lightbox-will-mount
  [own]
  (let [key (events/listen js/document
                           EventType.KEYDOWN
                           on-esc-clicked)]
    (assoc own ::key key)))

(defn- lightbox-will-umount
  [own]
  (events/unlistenByKey (::key own))
  (dissoc own ::key))

(defn- lightbox-transfer-state
  [old-own own]
  (assoc own ::key (::key old-own)))

(defn- lightbox-render
  [own]
  (let [data (rum/react lightbox-l)
        classes (classnames
                 :hide (nil? data)
                 :transparent (:transparent? data))]
    (html
     [:div.lightbox
      {:class classes
       :ref "parent"
       :on-click (partial on-out-clicked own)}
      (render-lightbox data)])))

(def lightbox
  (mx/component
   {:name "lightbox"
    :render lightbox-render
    :transfer-state lightbox-transfer-state
    :will-mount lightbox-will-mount
    :will-unmount lightbox-will-umount
    :mixins [rum/reactive mx/static]}))