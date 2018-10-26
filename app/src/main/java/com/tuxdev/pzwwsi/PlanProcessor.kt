package com.tuxdev.pzwwsi

import com.tuxdev.pzwwsi.Day
import com.tuxdev.pzwwsi.Lecture
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.util.CellUtil
import java.io.InputStream

// copy of excel file analyzer written before
class PlanProcessor {
    private val arr = ArrayList<ArrayList<String>>()
    private var ready = false

    fun load(file: InputStream) : Boolean {
        try {
            //val excelFile = FileInputStream(file)
            val workbook = HSSFWorkbook(file)
            val sheet = unmerge(workbook.getSheetAt(0))

            sheet.forEach {
                arr.add(arrayListOf())
                it.forEach {
                    arr.last().add(when {
                        it.cellTypeEnum === CellType.BLANK -> ""
                        it.cellTypeEnum === CellType.STRING -> it.stringCellValue
                        else -> it.numericCellValue.toString()
                    })
                }
            }

            // fucked up deleting empty lines
            val toDelete = ArrayList<ArrayList<String>>()
            arr.forEach {
                var flag = false
                it.forEach {
                    if (it.isNotBlank())
                        flag = true
                }
                if (!flag)
                    toDelete.add(it)
            }
            toDelete.forEach {
                arr.remove(it)
            }

            arr.first().add(0, "Dzień")

            workbook.close()
            //excelFile.close()
        }
        catch (e : Exception){
            return false
        }
        ready = true
        return true
    }

    fun isLoaded() : Boolean = !arr.isEmpty()

    fun hasGroup(group: String) : Boolean{
        for(i in 0 until arr.first().size) {
            if(arr.first()[i].contains(group,true)) {
                return true
            }
        }
        return false
    }

    fun getDays(group: String): ArrayList<Day>? {
        if(arr.isEmpty())
            return null

        val days = arrayListOf(Day("Poniedziałek"), Day("Wtorek"), Day("Środa"), Day("Piątek"))


        /*arr.forEach {
            it.forEach {
                print("${String.format("%15.15s", it)}|")
            }
            println()
        }*/

        var groupIndex = -1
        for(i in 0 until arr.first().size) {
            if(arr.first()[i].contains(group,true)) {
                groupIndex = i
                break
            }
        }

        if (groupIndex >= 0) {
            //var blok = 0

            var startRow = 1
            var endRow = 16

            for (d in 0 until days.size) {
                var blok = 0
                for (i in startRow until endRow) {
                    if (arr[i][groupIndex].isNotBlank()) {
                        //println("DATA: ${arr[i][firstIndex]} ${arr[i][firstIndex+1]}")

                        var rooms = arrayListOf(arr[i][groupIndex])
                        var data = arrayListOf(arr[i][groupIndex + 1])
                        var backindex = 1   // only used in while below

                        while (rooms.first() == data.first()) {
                            rooms = arrayListOf(arr[i][groupIndex - backindex])
                            backindex++
                        }

                        if (arr[i][groupIndex].contains("/")) {
                            rooms = arr[i][groupIndex].split("/") as ArrayList
                            data = arr[i][groupIndex + 1].split("/") as ArrayList

                            for (j in 0 until rooms.size) {
                                if (days[d].plan[blok] == null)
                                    days[d].plan[blok] = arrayListOf(Lecture(data[j], rooms[j].toDouble().toInt()))
                                else
                                    days[d].plan[blok]?.add(Lecture(data[j], rooms[j].toDouble().toInt()))
                            }
                        } else {
                            if (days[d].plan[blok] == null)
                                days[d].plan[blok] = arrayListOf(Lecture(data.first(), rooms.first().toDouble().toInt()))
                            else
                                days[d].plan[blok]?.add(Lecture(data.first(), rooms.first().toDouble().toInt()))
                        }
                    }
                    blok++
                }
                startRow += 15
                endRow += 15
            }
        }
        else
            return null

        return days
    }

    private fun unmerge(sheet: Sheet): Sheet {

        sheet.mergedRegions.forEach {
            val firstCell = CellUtil.getCell(CellUtil.getRow(it.firstRow, sheet), it.firstColumn)

            var dataS: String? = null
            var dataD: Double? = null

            if (firstCell.cellTypeEnum === CellType.STRING)
                dataS = firstCell.stringCellValue
            else
                dataD = firstCell.numericCellValue

            //println("DEBUG: ${it.firstColumn}  ${it.firstRow}  ${it.lastColumn}  ${it.lastRow}")

            for (i in it.firstColumn..it.lastColumn) {
                for (j in it.firstRow..it.lastRow) {
                    if (dataD == null)
                        CellUtil.getCell(CellUtil.getRow(j, sheet), i).setCellValue(dataS)
                    else
                        CellUtil.getCell(CellUtil.getRow(j, sheet), i).setCellValue(dataD)
                }
            }

            //println(it.numberOfCells)
        }

        while (sheet.numMergedRegions > 0) {
            for (i in 0 until sheet.numMergedRegions) {
                sheet.removeMergedRegion(i)
            }
        }

        return sheet
    }
}