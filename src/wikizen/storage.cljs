(ns wikizen.storage)

(defn- apply-delta
  "Applies delta to the passed wiki"
  [wiki delta]



  )

(defn get-wiki
  "Returns the wiki object"
  [id]
  { :name "SampleWiki"
    :root { :title "Root Page"
            :body "This is the *page body* of a fake wiki. And __this__ is `code`."
            :children [ { :title "Nested Page 1"
                          :body "The __content__ of _nested_ page 1"
                          :children [ { :title "Nested Page 1_1"
                                        :body "This _is_ a leaf" } ] }
                        { :title "Nested Page 2"
                          :body "The __content__ of _nested_ page 2"
                          :children [ { :title "Nested Page 2_1"
                                        :body "This _is_ a leaf" } ]} ] } })
(defn update-wiki
  "Applies the passed deltas"
  [id & deltas]
  [])
