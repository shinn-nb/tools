# 弹窗恶搞
import tkinter as tk
import random

# brew install python-tk

def dow():
    window = tk.Tk()
    width = window.winfo_screenwidth()
    height = window.winfo_screenheight()
    a = random.randrange(0, width)
    b = random.randrange(0, height)
    window.title('还款提醒')
    window.geometry("250x350" + "+" + str(a) + "+" + str(b))
    tk.Label(window,
             text='2021新年快乐！',  # 标签的文字
             bg='Red',  # 背景颜色
             font=('楷体', 17),  # 字体和字体大小
             width=15, height=2  # 标签长宽
             ).pack()  # 固定窗口位置
    tk.Button(window, text="已还", width=5, height=1, bg="red", relief="raised").pack()
    window.mainloop()


dow()
