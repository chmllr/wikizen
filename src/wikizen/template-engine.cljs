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

(defn- add-content
  "Appends content of an ellemen as DOM children"
  [element content]
  (cond 
    (string? content) (.appendChild element (.createTextNode js/document content))
    (instance? js/HTMLElement content) (.appendChild element content)
    (coll? content) (doseq [part content]
                      (add-content element part))
    (nil? content) nil
    :default (throw "Cannot add content:" content)))

(defn- html?
  "Returns true for alle vectors of type [<keyvord> ...]"
  [element]
  (and (vector? element)
       (keyword? (first element))))

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
  [input]
  (if (coll? input)
    (if (html? input)
      (let [[tag & more] input
            props (first (filter map? more))
            contents (map element->dom (remove map? more))
            id-present? (< -1 (.indexOf (name tag) "#"))
            [tag & id-and-classes] (string/split (name tag) #"[#|\.]")
            [id class] (if id-present?
                         [(first id-and-classes) (rest id-and-classes)]
                         [nil id-and-classes])
            props (merge props {:id id
                                :className (apply print-str class)})
            element (dom/createElement tag)]
        ; set props
        (doseq [[prop value] props]
          (when value
            (deep-set element prop value)))
        ; set contents
        (add-content element contents)
        element)
      (map element->dom input))
    input))

(defn template->dom
  "Converts a template into DOM"
  [& elements]
  (let [fragment (.createDocumentFragment js/document)]
    (doseq [elem (remove nil? elements)]
      (.appendChild fragment (element->dom elem)))
    fragment))
