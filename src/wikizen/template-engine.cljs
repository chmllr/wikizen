(ns wikizen.template-engine
  (:require [clojure.string :as string]
            [goog.dom :as dom]))

(defn- camelize
  "Turns this-name-token into thisNameToken"
  [token]
  (let [sub-tokens (clojure.string/split (name token) #"-")]
    (apply str
           (first sub-tokens)
           (map #(str (.toUpperCase (.charAt % 0))
                      (.slice % 1)) (rest sub-tokens)))))

(defn- deep-set
  "Recursively applies a setter"
  [element prop value]
  (if (map? value)
    (doseq [[k v] value]
      (deep-set
        (aget element (name prop))
        k v))
    (aset element (camelize prop) value)))

(defn- element->dom
  "Converts one template vector to a DOM element"
  [[tag & more]]
  (let [props (first (filter map? more))
        content (first (remove map? more))
        content (if (vector? content)
                  (element->dom content)
                  content)
        id-present? (< -1 (.indexOf (name tag) "#"))
        [tag & id-and-classes] (string/split (name tag) #"[#|\.]")
        [id class] (if id-present?
                     [(first id-and-classes) (rest id-and-classes)]
                     [nil id-and-classes])
        props (merge props {:id id
                            :className (apply print-str class)
                            :innerHTML content})
        element (dom/createElement tag)]
    (doseq [[prop value] props]
      (when value
        (deep-set element prop value)))
    element))

(defn- template->dom
  "Converts a template into DOM"
  [& elements]
  (let [fragment (.createDocumentFragment js/document)]
    (doseq [elem elements]
      (.appendChild fragment (element->dom elem)))
    fragment))