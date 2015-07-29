package com.fdt.sdl.admin.ui.controller.printq;

import static com.fdt.security.ui.SecurityViewConstants.JMESA_LOAD_PRINTQ_GRID_API;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.jmesa.model.AllItems;
import org.jmesa.model.TableModel;
import org.jmesa.model.WorksheetSaver;
import org.jmesa.view.html.component.HtmlColumn;
import org.jmesa.view.html.component.HtmlRow;
import org.jmesa.view.html.component.HtmlTable;
import org.jmesa.worksheet.Worksheet;
import org.jmesa.worksheet.WorksheetCallbackHandler;
import org.jmesa.worksheet.WorksheetColumn;
import org.jmesa.worksheet.WorksheetRow;
import org.jmesa.worksheet.WorksheetRowStatus;
import org.jmesa.worksheet.editor.CheckboxWorksheetEditor;
import org.jmesa.worksheet.editor.RemoveRowWorksheetEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fdt.common.entity.ErrorCode;
import com.fdt.sdl.admin.ui.controller.AbstractBaseSDLController;
import com.fdt.sdl.ws.client.PrintQFacadeService;
import com.fdt.sdl.ws.client.PrintQFacadeServiceImpl;
import com.fdt.sdl.ws.client.PrintQItem;
import com.fdt.sdl.ws.exception.PrintQServiceException;

@Controller
public class PrintQController extends AbstractBaseSDLController {
	
	private static String printQWsdl = null;
	
	private static PrintQFacadeService service = new PrintQFacadeServiceImpl();

	@RequestMapping(value="/addToPrintQ.admin", produces="application/json")
	@ResponseBody
	public List<ErrorCode> addToPrintQ(HttpServletRequest request,
					   	   HttpServletResponse response,
					   	   @RequestParam(required=false) String basketName,
					   	   @RequestParam(required=false) String seq_Key,
					   	   @RequestParam(required=false) String remarks,
					       @RequestParam(required=false) String book,
					   	   @RequestParam(required=false) String page,
					   	   @RequestParam(required=false) String pageCount,
					   	   @RequestParam(required=false) String noOfCopies,
					       HttpSession httpSession,
					   	   @RequestParam(defaultValue="false") boolean isImageAvailable) {
		String code = "ERROR";
		String description = "Required Information Missing.";
		ErrorCode errorCode = new ErrorCode();
		errorCode.setCode(code);
		errorCode.setDescription(description);
		if (!StringUtils.isBlank(basketName)) {
			errorCode =  addToPrintQ(request, basketName, book, page, remarks, seq_Key, code, description);			
		} else {
			basketName = getBasketNameFromSession(request);
			if (!StringUtils.isBlank(basketName)) {
				errorCode =  addToPrintQ(request, basketName, book, page, remarks, seq_Key, code, description);			
			}
		}
		return responseHandler(response, errorCode);
	}
	
	@RequestMapping(value="/loadPrintQGridAPI.admin")
	public ModelAndView loadPrintQGridAPI(HttpServletRequest request, HttpServletResponse response) throws PrintQServiceException{
		ModelAndView modelAndView = new ModelAndView(JMESA_LOAD_PRINTQ_GRID_API);
		String basketName = PrintQController.getBasketNameFromSession(request);					
		modelAndView.addObject("items", getTableModelOfGridString(request, false, basketName));
		modelAndView.addObject("request", request);
		return modelAndView;
	}
	
	@RequestMapping(value="/loadPrintQGridAPIAction.admin")
	public ModelAndView loadPrintQGridAPIAction(HttpServletRequest request, HttpServletResponse response) throws PrintQServiceException{
		ModelAndView modelAndView = new ModelAndView(JMESA_LOAD_PRINTQ_GRID_API);
		String basketName = PrintQController.getBasketNameFromSession(request);				
		modelAndView.addObject("items", getTableModelOfGridString(request, true, basketName));
		modelAndView.addObject("request", request);
		return modelAndView;
	}
	
	@RequestMapping(value="/KeepAlive.admin", produces="application/json")
    @ResponseBody
    public String keepAlive() {
        return "OK";
    }
	
	@RequestMapping(value="/closeBasket.admin", produces="application/json")
	@ResponseBody
	public String closeBasket(HttpServletRequest request, HttpServletResponse response) {
		 PrintQController.closeBasketInSession(request);
		 return "OK";
	}
	
	@RequestMapping(value="/createNewBasket.admin", produces="application/json")
	@ResponseBody
	public String createNewBasket(HttpServletRequest request, HttpServletResponse response) {
		 PrintQController.closeBasketInSession(request);
		 return "OK";
	}
	
	public static String getPrintQWsdl() {
		return printQWsdl;
	}

	public static void setPrintQWsdl(String printQWsdl) {
		PrintQController.printQWsdl = printQWsdl;
	}
	
	public static int getPrinQSize(HttpServletRequest request) {
		String basketName = PrintQController.getBasketNameFromSession(request);	
		int printQSize = 0;		
		try {
			PrintQItem printQItems [] = service.retrieveItems(printQWsdl, basketName);
			if (printQItems != null) {
				printQSize = printQItems.length;
			}
			
		} catch (PrintQServiceException e) {
			System.out.println(e.getErrorDescription());
		}
		return printQSize;
	}
	
	private static void closeBasketInSession(HttpServletRequest request) {
		HttpSession session = request.getSession();
		session.removeAttribute("PRINTQ_BASKET_NAME");		
	}
	
	private static ErrorCode addToPrintQ(HttpServletRequest request, String basketName, String book, String page, String remarks, 
			String seq_Key, String code, String description) {
		PrintQItem printQItem = new PrintQItem();
		if(!basketName.equalsIgnoreCase("null")) {			
			if (!StringUtils.isBlank(book) && !StringUtils.isBlank(page)) {
				if(!book.equalsIgnoreCase("null") && !page.equalsIgnoreCase("null")) {
					printQItem.setBkPg(book.concat("/").concat(page));
				}
			}				
			printQItem.setName(basketName);
			printQItem.setRemarks(remarks);
			printQItem.setPageCount(1);
			printQItem.setNoOfCopies(1);
			printQItem.setSelectedPages("1");
			printQItem.setSeq_Key(seq_Key);
			try {
				printQItem = service.createItem(printQWsdl, printQItem);
				populateBasketNameInSession(request, basketName);
				code = "SUCCESS";
				description = "Item added successfully to the basket.";
			} catch (PrintQServiceException e) {
				code = "ERROR";
				description = "WSDL Service Exception";
			}			
		}
		ErrorCode errorCode = new ErrorCode();
		errorCode.setCode(code);
		errorCode.setDescription(description);
		return errorCode;		
	}

	private static String getBasketNameFromSession(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String basketName = (String) session.getAttribute("PRINTQ_BASKET_NAME");
		return basketName;
	}
	
	private static void populateBasketNameInSession(HttpServletRequest request, String basketName) {
		HttpSession session = request.getSession();
		session.setAttribute("PRINTQ_BASKET_NAME", basketName);		
	}


	private String getTableModelOfGridString(HttpServletRequest request, boolean save, final String basketName)
			throws PrintQServiceException {
		if (!StringUtils.isBlank(basketName)) {
			try {
				/*final PrintQItem printQItems[] = service.retrieveItems(printQWsdl, basketName);
				final Map<String, PrintQItem> printQItemsMap = constructPrintQItemMap(Arrays
						.asList(printQItems));*/
				TableModel tableModel = new TableModel("worksheet", request);
				tableModel.setEditable(true);
				if (save) {
					tableModel.saveWorksheet(new WorksheetSaver() {
						public void saveWorksheet(Worksheet worksheet) {
							saveWorksheetChanges(worksheet, basketName);
						}
					});
				}
				tableModel.setItems(new AllItems() {
					public Collection<?> getItems() {
						PrintQItem printQItems[] = null;
						try {
							printQItems = service.retrieveItems(printQWsdl, basketName);
						} catch (PrintQServiceException e) {							
						}
						if(printQItems != null && printQItems.length > 0) {
							return Arrays.asList(printQItems);
						} else {
							List<PrintQItem> printQItemList = new LinkedList<PrintQItem>();
							return printQItemList;
						}
						
					}
				});				
				HtmlTable htmlTable = constructHTMLTable(basketName);
				tableModel.setTable(htmlTable);
				return tableModel.render();
			} catch (Exception e) {
				return e.getMessage() + " While Rendering PrintQ Items.";
			}
			
		} else {
			return "Basket Name Is Empty!";
		}
	}
	
	private Map<String, PrintQItem> constructPrintQItemMap(List<PrintQItem> printQItemList) {
		Map<String, PrintQItem> printQItemMap = new HashMap<String, PrintQItem>();
		if(printQItemList != null && printQItemList.size() > 0) {
			for(PrintQItem printQItem :printQItemList) {
				printQItemMap.put(printQItem.getID().toString(), printQItem);
			}
		}
			return printQItemMap;		
	}
	
	private HtmlTable constructHTMLTable(String basketName) {
		String caption = "PrintQ Items";
		if (!StringUtils.isBlank(basketName)) {
			caption = caption + " For Basket:" + basketName;
		}
		
		HtmlTable htmlTable = new HtmlTable().caption(caption).width(
				"600px");
		HtmlRow htmlRow = new HtmlRow().uniqueProperty("ID");
		htmlTable.setRow(htmlRow);
		HtmlColumn documentId = new HtmlColumn("seq_Key")
				.title("Document ID");
		documentId.setFilterable(false);
		documentId.setEditable(false);
		htmlRow.addColumn(documentId);
		
		HtmlColumn pageCount = new HtmlColumn("pageCount")
				.title("Total Pages");
		pageCount.setFilterable(false);
		pageCount.setEditable(false);
		htmlRow.addColumn(pageCount);
		
		HtmlColumn bkPg = new HtmlColumn("bkPg").title("Bk/Pg");
		bkPg.setFilterable(false);
		bkPg.setEditable(false);
		htmlRow.addColumn(bkPg);
		
		HtmlColumn noImage = new HtmlColumn("noImage").title("No Image");
		noImage.setFilterable(false);
		noImage.setEditable(false);
		htmlRow.addColumn(noImage);
		
		HtmlColumn remarks = new HtmlColumn("remarks").title("Remarks");
		remarks.setFilterable(false);
		htmlRow.addColumn(remarks);
		
		HtmlColumn noOfCopies = new HtmlColumn("noOfCopies")
				.title("Copies");
		noOfCopies.setFilterable(false);
		htmlRow.addColumn(noOfCopies);
		
		HtmlColumn certified = new HtmlColumn("certified").title("Certify");
		certified.setWorksheetEditor(new CheckboxWorksheetEditor());
		certified.filterable(false).sortable(false);
		htmlRow.addColumn(certified);
		
		HtmlColumn remove = new HtmlColumn("remove");
		remove.setWorksheetEditor(new RemoveRowWorksheetEditor());
		remove.setTitle("&nbsp;");
		remove.setFilterable(false);
		remove.setSortable(false);
		htmlRow.addColumn(remove);
		return htmlTable;
	}

	protected void saveWorksheetChanges(Worksheet worksheet, final String basketName) {
		    PrintQItem printQItems[] = null;
		   	try {
				printQItems = service.retrieveItems(printQWsdl, basketName);				
			} catch (PrintQServiceException e) {
				String msg = "PrintQServiceException when Retrieving Items From Basket to Load Worksheet " + e.getMessage();
				throw new RuntimeException(msg);
			}
			final Map<String, PrintQItem> printQItemsMap = constructPrintQItemMap(Arrays.asList(printQItems));
	        worksheet.processRows(new WorksheetCallbackHandler() {
	            public void process(WorksheetRow worksheetRow) {
	                if (worksheetRow.getRowStatus().equals(WorksheetRowStatus.ADD)) {
	                	try {
							service.deleteBasket(printQWsdl, basketName);
						} catch (PrintQServiceException e) {
							String msg = "PrintQServiceException when Deleting Basket From Worksheet " + e.getMessage();
							throw new RuntimeException(msg);
						}
	                } else if (worksheetRow.getRowStatus().equals(WorksheetRowStatus.REMOVE)) {
	                	 String uniqueValue = worksheetRow.getUniqueProperty().getValue();
	                	 PrintQItem printQItem = new  PrintQItem();
	                	 printQItem.setID(Integer.valueOf(uniqueValue));
	                	 printQItem.setName(basketName);
	                	 try {
							service.deleteItem(printQWsdl, printQItem);
						} catch (PrintQServiceException e) {
							String msg = "PrintQServiceException when Deleting Item From basket In Worksheet " + e.getMessage();
							throw new RuntimeException(msg);
						}
	                   
	                } else if (worksheetRow.getRowStatus().equals(WorksheetRowStatus.MODIFY)) {
	                    Collection<WorksheetColumn> columns = worksheetRow.getColumns();
	                    for (WorksheetColumn worksheetColumn : columns) {
	                        String changedValue = worksheetColumn.getChangedValue();
	                        validateColumn(worksheetColumn, changedValue);
	                        if (worksheetColumn.hasError()) {
	                            continue;
	                        }
	                        String uniqueValue = worksheetRow.getUniqueProperty().getValue();
	                        PrintQItem printQItem = printQItemsMap.get(uniqueValue);
	                        String property = worksheetColumn.getProperty();

	                        try {
	                            if (worksheetColumn.getProperty().equals("certified")) {
	                            if (changedValue.equals(CheckboxWorksheetEditor.CHECKED)) {
	                                    PropertyUtils.setProperty(printQItem, property, "y");
	                                } else {
	                                    PropertyUtils.setProperty(printQItem, property, "n");
	                                }

	                            } else {	                                
	                                if(!StringUtils.isBlank(property) && property.equalsIgnoreCase("noOfCopies")) {
	                                	printQItem.setNoOfCopies(changedValue);
	                                } else {
	                                	PropertyUtils.setProperty(printQItem, property, changedValue);
		                                if(!StringUtils.isBlank(property) && property.equalsIgnoreCase("pageCount")) {
		                                	printQItem.setSelectedPages(getSelectedPages(changedValue));
		                                }
	                                }
	                            }
	                        } catch (Exception ex) {
	                            String msg = ex.getMessage() + " Not able to set the property [" + property + "] when updating worksheet.";
	                            throw new RuntimeException(msg);
	                        }

	                        try {
								service.updateItem(printQWsdl, printQItem);
							} catch (PrintQServiceException e) {
								String msg = "PrintQServiceException when updating worksheet " + e.getMessage();
								throw new RuntimeException(msg);
							}
	                    }
	                }
	            }

	            private  String getSelectedPages(String changedValue) {
	        		try {
	        			int pageCount = Integer.valueOf(changedValue);
	        			String selectedPages = "";
	        			for(int i=1; i < pageCount; i++) {
	        				if(i <= 5) {
	        					selectedPages = selectedPages + i + ",";
	        				} 				
	        			}
	        			
	        			if(pageCount > 7) {
	        				selectedPages = selectedPages + "..." + pageCount;
	        			} else {
	        				selectedPages = selectedPages + pageCount;
	        			}
	        			return selectedPages;
	        		} catch (Exception e) {
	        			return "0";
	        		}		
	        	}
	        });
	    }
		
		private void validateColumn(WorksheetColumn worksheetColumn, String changedValue) {
	        if (changedValue.equals("foo")) {
	            worksheetColumn.setErrorKey("foo.error");
	        } else {
	            worksheetColumn.removeError();
	        }
	    }
	private List<ErrorCode> responseHandler(HttpServletResponse response, ErrorCode error) {
		List<ErrorCode> errors = new LinkedList<ErrorCode>();
		response.reset();
		response.resetBuffer();
		errors.add(error);
		return errors;
	}	
}
