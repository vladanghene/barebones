(ns barebones.admin.calendar.utils)

(def size 15)
(def color-shades ["#eff3ff" "#c6dbef" "#9ecae1" "#6baed6" "#3182bd" "#08519c"])
(def days ["Sun" "Mon" "Tue" "Wed" "Thu" "Fri" "Sat"])

(defn choose-shade
  "Choose shade given the count data.

  `highest-count` is the highest count among the entire data.

  `count-data` is the count data used to select the shade."
  [highest-count count-data]
  (let [position (if (or (zero? highest-count)
                         (nil? highest-count))
                   0
                   (Math/ceil (* (/ (Math/ceil count-data)
                                    highest-count)
                                 (dec (count color-shades)))))]
    (nth color-shades position)))

(defn find-placement
  "Find/calculate placement for item according to its index. Works for both
  horizontal and vertical placement.

  `idx` is the index of item for placement.

  Takes optional :offset key to offset placement."
  [idx & {:keys [offset] :or {offset 0}}]
  (+ (+ offset (* 3 idx)) (* size (inc idx))))

(defn find-details
  "Find details for given day from data.

  `highest-count` is the highest count among the entire data.

  `year-data` is a hashmap of date string to count.

  `day` is the day of the year."
  [highest-count year-data day]
  (let [date (-> (js/moment) (.startOf "day") (.dayOfYear day))
        date-string (.format date "YYYY-MM-DD")
        date-display (.format date "Do MMMM YYYY")
        day (.day date)
        count-data (get year-data date-string 0)]
    {:day day
     :date-obj date
     :date date-string
     :count count-data
     :shade (choose-shade highest-count count-data)
     :y (find-placement day)
     :size size
     :title (case count-data
              0 (str "No issues on " date-display)
              1 (str count-data " issue on " date-display)
              (str count-data " issues on " date-display))}))

(defn add-offset
  "Offset the list with nils so the first day of the week will always starts at
  Sunday."
  [details]
  (let [{:keys [day]} (first details)]
    (concat (repeat day nil) details)))

(defn assoc-day-key
  "Associate React key to each day object.

  `idx` is week index.

  `day` is the day object/hashmap."
  [idx day]
  (assoc day :key (str "week" idx "-day" (:day day))))

(defn find-details-for-year-data
  "Find details for each day in year from year-data.

  `year-data` is a hashmap of date string to count."
  [year-data]
  (let [today (.dayOfYear (js/moment))
        last-year (- today 365)
        days (range last-year (inc today)) ; include today
        highest-count (apply max (vals year-data))]
    (map (partial find-details highest-count year-data) days)))

(defn generate-calendar-heatmap-data
  "Generate data representation for calendar heatmap. The first week will be
  offset so that the first day of the week will always starts at Sunday.

  `year-data-details` is a list of details for each day."
  [year-data-details]
  (let [weeks (partition-all 7 (add-offset year-data-details))]
    (map-indexed (fn [idx week]
                   (let [x (find-placement idx :offset 31)]
                     {:key (str "week" idx)
                      :transform (str "translate(" x ", 0)")
                      :week idx
                      :details (map (partial assoc-day-key idx) week)
                      :x x}))
                 weeks)))

(defn generate-months-legend-data
  "Generate data representation for months legend of calendar heatmap. The data is
  a list of mapping between the position and month name.

  `heatmap-data` is the data generated by `generate-calendar-heatmap-data`
  function."
  [heatmap-data]
  (->> heatmap-data
       (map-indexed #(vector %1 (:date (first (:details %2)))))
       (partition-by (fn [[_ date]] (.month (js/moment date))))
       (filter #(>= (count %) 4))  ; only display legend for fully visible month
       (map #(let [[idx date] (first %)] ; take first week
               {:key (str "monthlegend" idx)
                :idx idx
                :month (-> (js/moment date) (.format "MMM"))
                :x (find-placement idx)}))))

(defn generate-days-legend
  "Generate React key, index, name, and y postition for days legend."
  []
  (map-indexed (fn [idx day]
                 {:key (str "daylegend" idx)
                  :idx idx
                  :day day
                  :y (find-placement idx)})
               days))
