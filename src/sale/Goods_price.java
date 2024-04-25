/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sale;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

/**
 *
 * @author Binh
 */
public final class Goods_price {
    
    public static Vector ma_sp;
    public static Vector ten_sp;
    public static Vector giaS_sp;
    public static Vector giaL_sp;
    public static Vector moc_sp;
    public static Vector soluong_sp;
    public static Vector nickname_sp;
    public static Vector sale_distribution;
    public static FileInputStream myInput;
    public static FileOutputStream myOutput;
    public static POIFSFileSystem myFileSystem;
    public static HSSFWorkbook myWorkBook;
    public static HSSFSheet mySheet;
    public static Iterator<Row> rowIterator;
    public static Row row;
    public static Iterator<Cell> cellIterator;
    public static Cell cell   ;
    int MAX_FIND = 100;
   public  Goods_price() {
       ma_sp        = new Vector();
       ten_sp       = new Vector();
       giaS_sp      = new Vector();
       giaL_sp      = new Vector();
       moc_sp       = new Vector();
       soluong_sp   = new Vector();
       nickname_sp  = new Vector();
       sale_distribution    = new Vector();
       int size_sp;
       int i;
       
       System.out.print("Enter initial data()\n");
              

        
       //Read an Excel File and Store in a Vector
       readExcelFile(parameter.goods_info);
               
       //Print the data read
        size_sp = ma_sp.size();
        for(i = 0; i < size_sp; i++)
        {
            display_sp(i);
        }
        System.out.print("exit initial data()\n");
   }
   
   public static int readExcelFile(String good_excelfile)
   {
               
       int count;
        try{
            /** Creating Input Stream**/
            myInput = new FileInputStream(good_excelfile);

            /** Create a POIFSFileSystem object**/
            myFileSystem = new POIFSFileSystem(myInput);

            /** Create a workbook using the File System**/
            myWorkBook = new HSSFWorkbook(myFileSystem);

            /** Get the first sheet from workbook**/
            mySheet = myWorkBook.getSheetAt(0);
            /** We now need something to iterate through the cells.**/
            //Iterate through each rows from first sheet
            rowIterator = mySheet.iterator();
            boolean first = true;
            while(rowIterator.hasNext()) {
                row = rowIterator.next();
                if(first)
                {
                    first = false;
                    continue;
                }

                //For each row, iterate through each columns
                cellIterator = row.cellIterator();
                //System.out.println("[]" + row.getRowNum());
                if(cellIterator.hasNext()){
                    for(count = 0; count < 8; count++) {
                        cell = cellIterator.next();
                        //System.out.println(count +"[]" + row.getRowNum());
                        switch (count) {
                            case 0:
                                //ma san pham
                                ma_sp.add(cell.getStringCellValue());
                                break;
                            case 1:
                                //ten san pham
                                ten_sp.add(cell.getStringCellValue());
                                break;
                            case 2:
                                //gia s san pham
                                giaS_sp.add(cell.getNumericCellValue());
                                break;
                            case 3:
                                //gia l san pham
                                giaL_sp.add(cell.getNumericCellValue());
                                break;
                            case 4:
                                //moc san pham
                                moc_sp.add((int)cell.getNumericCellValue());
                                break;
                            case 5:
                                //so luong san pham
                                soluong_sp.add((int)cell.getNumericCellValue());
                                break;
                            case 6:
                                //sale distribution san pham
                                sale_distribution.add(cell.getNumericCellValue());
                                break;
                            case 7:
                                //nickname san pham
                                nickname_sp.add(cell.getStringCellValue());
                                break;
                            default:
                                break;
                        }
                
                    }
                
                }
                
            }
            myInput.close();
        }
        catch (FileNotFoundException e) {
            return -1;
        } 
        catch (IOException e) {
            return -1;
        }
                
        return 1;
    }

   public void display_sp(int i)
   {
        System.out.println("[" + i + "]" +ma_sp.elementAt(i) + "\t" + 
                ten_sp.elementAt(i) + "\t" +
                giaS_sp.elementAt(i) + "\t" +
                giaL_sp.elementAt(i) + "\t" +
                soluong_sp.elementAt(i) + "\t" +
                nickname_sp.elementAt(i));
    }

   public int update_data(Vector new_update_SP)
   {
       int i, return_val;
       i = tim_sp(new_update_SP.get(0).toString());
       System.out.println("tim dc san pham index = " + i + " MASP: " + new_update_SP.get(0).toString());
       check_data_size("before update");
       if(i == -1)      //not found
       {
           return_val = added_sp(new_update_SP);
       }else
       {
           return_val = update_sp(i,new_update_SP);
       }
       check_data_size("after update data");
       return return_val;
   }
   public void check_data_size(String imessage)
   {
      System.out.println(imessage + " size of each vector :" + ma_sp.size() + "\t" +
       ten_sp.size()+ "\t" + giaS_sp.size() + "\t" + giaL_sp.size() + "\t"
       + moc_sp.size() + "\t" + soluong_sp.size() + "\t"+ sale_distribution.size() +
       "\t" + nickname_sp.size());
   }
   public int added_sp(Vector newSP)
   {
       int  return_val = 0;
       mySheet = myWorkBook.getSheetAt(0);
       row = mySheet.createRow(row.getRowNum()+1);
       cell = row.createCell(0);
       cell.setCellValue(newSP.get(0).toString());
       cell = row.createCell(1);
       cell.setCellValue(newSP.get(1).toString());
       cell = row.createCell(2);
       cell.setCellValue(Double.parseDouble(newSP.get(2).toString()));
       cell = row.createCell(3);
       cell.setCellValue(Double.parseDouble(newSP.get(3).toString()));
       cell = row.createCell(4);
       cell.setCellValue(Integer.parseInt("5"));
       cell = row.createCell(5);
       cell.setCellValue(Integer.parseInt(newSP.get(4).toString()));
       cell = row.createCell(6);
       cell.setCellValue(Double.parseDouble("0.0"));
       cell = row.createCell(7);
       cell.setCellValue(newSP.get(1).toString());
       //add to vector
       ma_sp.add(newSP.get(0).toString());
       ten_sp.add(newSP.get(1).toString());
       giaS_sp.add(newSP.get(2).toString());
       giaL_sp.add(newSP.get(3).toString());
       moc_sp.add(Integer.parseInt("5"));
       soluong_sp.add(Integer.parseInt(newSP.get(4).toString()));
       sale_distribution.add(Double.parseDouble("0"));
       nickname_sp.add(newSP.get(1).toString());
        try {
            myOutput = new FileOutputStream(parameter.goods_info);
            myWorkBook.write(myOutput);
            myOutput.close();
            
        } catch (FileNotFoundException ex) {
            return_val = -1;
            Logger.getLogger(Goods_price.class.getName()).log(Level.SEVERE, null, ex);
            
        } catch (IOException ex) {
            return_val = -2;
            Logger.getLogger(Goods_price.class.getName()).log(Level.SEVERE, null, ex);
        }
        return return_val;
        //System.out.println("Excel written successfully..");
   }
    
   public int update_sp(int i, Vector updateSP)
   {
       int  sl_sp_new  = Integer.parseInt(updateSP.get(4).toString());
       //int  sl_sp_old  = Integer.parseInt(soluong_sp.get(i).toString());
       //System.out.println("old " + sl_sp_old + " new " + sl_sp_new);
       //sl_sp_new       = sl_sp_new + sl_sp_old;
       //System.out.println("old " + sl_sp_old + " new " + sl_sp_new);
       mySheet = myWorkBook.getSheetAt(0);
       ten_sp.set(i, updateSP.get(1));
       giaS_sp.set(i, updateSP.get(2));
       giaL_sp.set(i, updateSP.get(3));
       soluong_sp.set(i, sl_sp_new);
       
       //produces name
       cell = mySheet.getRow(i +1).getCell(1);
       cell.setCellValue(updateSP.get(1).toString());
       
       //price 1 
       cell = mySheet.getRow(i +1).getCell(2);
       cell.setCellValue(Double.parseDouble(updateSP.get(2).toString()));
       //price 2
       cell = mySheet.getRow(i +1).getCell(3);
       cell.setCellValue(Double.parseDouble(updateSP.get(3).toString()));
       
       //number of procuces
       cell = mySheet.getRow(i + 1).getCell(5);
       cell.setCellValue(sl_sp_new);

       try {
            myOutput = new FileOutputStream(parameter.goods_info);
            myWorkBook.write(myOutput);
            myOutput.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Goods_price.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        } catch (IOException ex) {
            Logger.getLogger(Goods_price.class.getName()).log(Level.SEVERE, null, ex);
            return -2;
        }       
       return 0;
   }
    
   public int tim_sp(String iMa_sp)
   {
       int i;
       int size = ma_sp.size();
       System.out.println("finding :" + iMa_sp);
       for(i =0; i <size; i++)
       {
           if(ma_sp.elementAt(i).equals(iMa_sp))
           {
               System.out.println("[" +i +"] Matched");
               return i;
           }
       }
       System.out.println("Not Found!");
       return -1;
   }
   
   public void  update_distribution(String iMaSP)
   {
       int i = tim_sp(iMaSP);
       double new_sale_dis_val ;
       if(i != -1)
       {
           new_sale_dis_val = Double.parseDouble(sale_distribution.get(i).toString()) + 0.01;
           sale_distribution.removeElementAt(i);
           sale_distribution.add(i, new_sale_dis_val);
       }
   }
   public int   reorder_file_data()
   {
       int cur_max_index = 0;
       double   cur_max_val = 0;
       Vector data_row  = new Vector();
       int return_val = 0;
       int data_count  = ma_sp.size();
       int i,j;
       for(i = 0; i < data_count; i++)
       {
           cur_max_val = Double.parseDouble(sale_distribution.get(i).toString());
           cur_max_index = i;
           System.out.println("finish at : " + i + " data count " + data_count);
           for( j = i + 1; j < data_count; j++)
           {
               if(cur_max_val < Double.parseDouble(sale_distribution.get(j).toString()))
               {
                   cur_max_val  = Double.parseDouble(sale_distribution.get(j).toString());
                   cur_max_index = j;
               }
           }
               
    
           if(i != cur_max_index)
           {
               update_file_data_row(i + 1, cur_max_index);
               update_file_data_row(cur_max_index + 1, i);
               
               data_row.removeAllElements();
               data_row.add(ma_sp.get(cur_max_index));
               data_row.add(ten_sp.get(cur_max_index));
               data_row.add(giaS_sp.get(cur_max_index));
               data_row.add(giaL_sp.get(cur_max_index));
               data_row.add(moc_sp.get(cur_max_index));
               data_row.add(soluong_sp.get(cur_max_index));
               data_row.add(sale_distribution.get(cur_max_index));
               data_row.add(nickname_sp.get(cur_max_index));
               ma_sp.set(cur_max_index,ma_sp.get(i));
               ten_sp.set(cur_max_index,ten_sp.get(i));
               giaS_sp.set(cur_max_index,giaS_sp.get(i));
               giaL_sp.set(cur_max_index,giaL_sp.get(i));
               moc_sp.set(cur_max_index,moc_sp.get(i));
               soluong_sp.set(cur_max_index,soluong_sp.get(i));
               sale_distribution.set(cur_max_index,sale_distribution.get(i));
               nickname_sp.set(cur_max_index,nickname_sp.get(i));
               ma_sp.set(i,data_row.get(0));
               ten_sp.set(i,data_row.get(1));
               giaS_sp.set(i,data_row.get(2));
               giaL_sp.set(i,data_row.get(3));
               moc_sp.set(i,data_row.get(4));
               soluong_sp.set(i,data_row.get(5));
               sale_distribution.set(i,data_row.get(6));
               nickname_sp.set(i,data_row.get(7));
               //update cell according
           }
       }
       return return_val;
   }
   
   public int update_file_data_row(int index_sheet, int ivector_index)
   {
       int return_val = 0;
       
       System.out.println("data update sheet " + index_sheet +  " vs " + ivector_index);
       mySheet = myWorkBook.getSheetAt(0);
              
       //produces code
       cell = mySheet.getRow(index_sheet).getCell(0);
       cell.setCellValue(ma_sp.get(ivector_index).toString());
       
       //produces name
       cell = mySheet.getRow(index_sheet).getCell(1);
       cell.setCellValue(ten_sp.get(ivector_index).toString());
       
       //price 1 
       cell = mySheet.getRow(index_sheet).getCell(2);
       cell.setCellValue(Double.parseDouble(giaS_sp.get(ivector_index).toString()));
       //price 2
       cell = mySheet.getRow(index_sheet).getCell(3);
       cell.setCellValue(Double.parseDouble(giaL_sp.get(ivector_index).toString()));
       //price 2
       cell = mySheet.getRow(index_sheet).getCell(4);
       cell.setCellValue(Integer.parseInt(moc_sp.get(ivector_index).toString()));
              //price 2
       cell = mySheet.getRow(index_sheet).getCell(5);
       cell.setCellValue(Integer.parseInt(soluong_sp.get(ivector_index).toString()));
              //price 2
       cell = mySheet.getRow(index_sheet).getCell(6);
       cell.setCellValue(Double.parseDouble(sale_distribution.get(ivector_index).toString()));
       //number of procuces
       cell = mySheet.getRow(index_sheet).getCell(7);
       cell.setCellValue(nickname_sp.get(ivector_index).toString());

       try {
            myOutput = new FileOutputStream(parameter.goods_info);
            myWorkBook.write(myOutput);
            myOutput.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Goods_price.class.getName()).log(Level.SEVERE, null, ex);
            return_val = -1;
        } catch (IOException ex) {
            Logger.getLogger(Goods_price.class.getName()).log(Level.SEVERE, null, ex);
            return_val = -2;
        }
       return return_val;
   }
   
   public Vector timThongMinh_sp(String iTenSP)
   {
       Vector index_result = new Vector(10);
       int find_index   = 0;
       int  max_sp  = ma_sp.size();
       for(int i = 0; i < max_sp; i++)
       {
           if(find_index >= MAX_FIND)
           {
               break;
           }
           //System.out.println("ten :" + ten_sp.get(i).toString().toLowerCase() + "[]" + iTenSP);
           if(nickname_sp.get(i).toString().toLowerCase().contains(iTenSP.toLowerCase()))
           {
               //System.out.println("found :" + nickname_sp.get(i).toString().toLowerCase());
               index_result.add(i);
               find_index ++;
           }
           else if(ten_sp.get(i).toString().toLowerCase().contains(iTenSP.toLowerCase()))
           {
               //System.out.println("found :" + ten_sp.get(i).toString().toLowerCase());
               index_result.add(i);
               find_index ++;
           }
       }
       return  index_result;
   }
   
   public Vector timThongMinh_MaSP(String iMaSP)
   {
       Vector index_result = new Vector(10);
       int find_index   = 0;
       int  max_sp  = ma_sp.size();
       for(int i = 0; i < max_sp; i++)
       {
           if(find_index >= MAX_FIND)
           {
               break;
           }
           //System.out.println("ten :" + ten_sp.get(i).toString().toLowerCase() + "[]" + iTenSP);
           if(ma_sp.get(i).toString().toLowerCase().contains(iMaSP.toLowerCase()))
           {
               //System.out.println("found :" + nickname_sp.get(i).toString().toLowerCase());
               index_result.add(i);
               find_index ++;
           }
       }
       return  index_result;
       
   }

   public Vector get_sp(int i)
   {
       Vector return_data = new Vector();
       return_data.add(ma_sp.elementAt(i));
       return_data.add(ten_sp.elementAt(i));
       return_data.add(Double.parseDouble(giaS_sp.elementAt(i).toString()));
       return_data.add(Double.parseDouble(giaL_sp.elementAt(i).toString()));
       return_data.add(Integer.parseInt(soluong_sp.elementAt(i).toString()));
       System.out.println("get san pham: " + i);
       System.out.println(ma_sp.elementAt(i) + "\t" + ten_sp.elementAt(i) + "\t" 
               + giaS_sp.elementAt(i) + "\t" + giaL_sp.elementAt(i) + "\t" + 
                  soluong_sp.elementAt(i));
       return return_data;
   }
   
   public int getCount_SP()
   {
       return ma_sp.size();
   }
}
