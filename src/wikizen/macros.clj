(ns wikizen.macros)

(defmacro link
  "Produces a link which sends the passed event to the event processor"
  [channel event label]
  `[:a {:href "#"
        :onclick
              #(do
                (.preventDefault %)
                (~channel ~event))}
    ~label])