(ns wikizen.events
  (:require [wikizen.log :as log]))

(defn delay-channel
  "Collects all events and puts some of them to the
  channel after the specified delay has passed"
  [channel delay buffer-events?]
  (let [timer (atom nil)
        events (atom [])]
    (fn [event]
      (log/! "event" (event :id) "sent to delayed channel with delay" delay)
      (js/clearTimeout @timer)
      (if buffer-events?
        (swap! events conj event)
        (reset! events [event]))
      (reset! timer
              (js/setTimeout
                #(do
                  (log/! "timeout executed successfully after" delay "ms")
                  (doseq [e @events] (channel e)))
                delay)))))