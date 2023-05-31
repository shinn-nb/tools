import pdfplumber
import re
import os
import pandas
import shutil
# 读取pdf发票信息对 文件名称进行转换

# 简单写法
def get_pdf(dir_path):
    pdf_file = []
    for root, sub_dirs, file_names in os.walk(dir_path): # 返回值:(dirpath, dirnames, filenames)
        for name in file_names:
            if name.endswith('.pdf'):
                filepath = os.path.join(root, name)
                pdf_file.append(filepath)
    return pdf_file

def read(pdf_dir,out_dir):
    pdfs = []
    filenames = get_pdf(pdf_dir)  # 修改为自己的文件目录
    for filename in filenames:
        pdf_f = {}
        with pdfplumber.open(filename) as pdf:
            first_page = pdf.pages[0]
            pdf_text = first_page.extract_text() # 提取文本信息
            pdf_table = first_page.extract_table() # 提取表格信息
            pdf_table = pandas.DataFrame(pdf_table)
            pdf_text = pdf_text.replace(' ', '').replace('　', '').replace('）', '').replace(')', '').replace('：', ':')
            # 正则匹配
            pdf_f["t1"] = re.search(r'[\u4e00-\u9fa5]+电子普通发票.*?',pdf_text).group()
            t2 = re.match(r'[\u4e00-\u9fa5]+专用发票.*?', pdf_text)
            if t2:
                pdf_f["t2"] = t2.group()
            pdf_f["pro_name"] = pdf_table.loc[1][0].replace("\n","").replace("合 计","")[4:] # 项目名称
            pdf_f["code"] = re.findall(r'发票代码:(.*\d+)', pdf_text)[0] # 发票代码
            pdf_f["num"] = re.findall(r'发票号码:(.*\d+)', pdf_text)[0] # 发票号码
            pdf_f["date"] = re.findall(r'开票日期:(.*)', pdf_text)[0] # 开票日期
            pdf_f["year"] = re.findall(r"([0-9]+\.?[0-9]+)",pdf_f["date"])[0]
            pdf_f["month"] = re.findall(r"([0-9]+\.?[0-9]+)",pdf_f["date"])[1]
            pdf_f["day"] = re.findall(r"([0-9]+\.?[0-9]+)",pdf_f["date"])[2]
            pdf_f["client_name"] = re.findall(r'名\s*称\s*[:]\s*([\u4e00-\u9fa5]+)', pdf_text)[0] # 购买方名称
            pdf_f["client_itin"] = re.findall(r'纳税人识别号\s*[:]\s*([a-zA-Z0-9]+)', pdf_text)[0] # 购买方税号
            pdf_f["seller_name"] = re.findall(re.compile(r'名.*称\s*[:]\s*([\u4e00-\u9fa5]+)'), pdf_text)[-1] # 销售方名称
            pdf_f["seller_itin"] = re.findall(r'纳税人识别号\s*[:]\s*([a-zA-Z0-9]+)', pdf_text)[-1] # 购买方税号
            pdf_f["car_num"] = pdf_table.loc[1][2].replace("车牌号\n","")
            pdf_f["car_type"] = pdf_table.loc[1][3].replace("类型\n","")
            pdf_f["total_price"] = re.findall(r"([0-9]+\.?[0-9]+)",pdf_table.loc[2][2])[0]
            pdf_f["price"] = re.findall(r"([0-9]+\.?[0-9]+)",pdf_table.loc[1][8])[0]
            pdf_f["tax_rate"] = pdf_table.loc[1][9].replace("税率\n","")
            pdf_f["tax_price"] = re.findall(r"([0-9]+\.?[0-9]+)",pdf_table.loc[1][10])[0]
            pdf_f["dir"] = filename
            pdf_f["path"] = pdf_dir
            pdf_f["file"] = filename.replace(pdf_dir,"")
        pdfs.append(pdf_f)
        out_file_name=pdf_f["num"]+"-"+pdf_f["total_price"]+"-"+ pdf_f["date"]
        dst = os.path.join(out_dir, out_file_name + ".pdf")
        shutil.copy(filename, dst)
    return pdfs
pdfs = []
# root_dir = "/Users/anan/Documents/pdf/input"
# out_dir = "/Users/anan/Documents/pdf/output"
root_dir=str(input("请输入发票所在目录："))
out_dir=str(input("请输入发票修改后存储目录："))
pdfs.extend(read(root_dir.strip(),out_dir.strip()))

print(f"共提取 {len(pdfs)} 张发票的信息.")