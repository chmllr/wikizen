(ns wikizen.template-engine
  (:require [clojure.string :as string]
            [goog.dom :as dom]))

(defn- camelize
  "Turns this-name-token into thisNameToken"
  [token]
  (let [sub-tokens (clojure.string/split (name token) #"-")]
    (apply str
           (first sub-tokens)
           (map string/capitalize (rest sub-tokens)))))

(defn- create-span
  "Creates a simple <span> element with content"
  [content]
  (let [span (dom/createElement "span")]
    (aset span "innerHTML" content)
    span))

(defn- add-content
  "Appends content of an ellemen as DOM children"
  [element content]
  (cond
    (instance? js/HTMLElement content) (.appendChild element content)
    (coll? content) (doseq [part content]
                      (add-content element part))
    (nil? content) :no-op
    :default (if (string/blank? (aget element "innerHTML"))
               (aset element "innerHTML" (str content))
               (.appendChild element (create-span content)))))

(defn- html?
  "Returns true for alle vectors of type [<keyword> ...]"
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
          (when-not (string/blank? value)
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
    (doseq [elem (map #(if-not (html? %)
                        (create-span %)
                        (element->dom %)) elements)]
      (.appendChild fragment elem))
    fragment))
