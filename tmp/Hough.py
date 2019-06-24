import cv2
import numpy as np

# カレンダー
img = cv2.imread("calendar.png")
img2 = img.copy()
img3 = img.copy()

# グレースケール
gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
cv2.imwrite("calendar_mod.png", gray)

## 反転 ネガポジ変換
gray2 = cv2.bitwise_not(gray)
cv2.imwrite("calendar_mod2.png", gray2)
lines = cv2.HoughLinesP(gray2, rho=1, theta=np.pi/360, threshold=80, minLineLength=80, maxLineGap=5)

for line in lines:
    x1, y1, x2, y2 = line[0]

    # 赤線を引く
    red_lines_img = cv2.line(img2, (x1,y1), (x2,y2), (0,0,255), 3)
    cv2.imwrite("calendar_mod3.png", red_lines_img)

    # 線を消す(白で線を引く)
    no_lines_img = cv2.line(img3, (x1,y1), (x2,y2), (255,255,255), 3)
    cv2.imwrite("calendar_mod4.png", no_lines_img)