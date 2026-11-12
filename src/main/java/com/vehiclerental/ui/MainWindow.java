package com.vehiclerental.ui;

import com.vehiclerental.contract.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.table.*;

/** Swing presentation layer; all data belongs to the existing domain through the desktop adapter. */
public final class MainWindow extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final Color NAVY=new Color(20,39,57), TEAL=new Color(7,119,111),
            BG=new Color(242,246,249), INK=new Color(29,49,66), MUTED=new Color(98,116,131);
    private final RentalDesktopModel model;
    private final CardLayout pages=new CardLayout();
    private final JPanel content=new JPanel(pages);
    private final JLabel heading=new JLabel(),subtitle=new JLabel(),status=new JLabel("Ready");
    private final JLabel[] metrics={new JLabel(),new JLabel(),new JLabel(),new JLabel()};
    private final DefaultTableModel fleetData=tableModel("ID","Vehicle","Type","Rate / hour","Status");
    private final DefaultTableModel rentalData=tableModel("Rental ID","Customer","Vehicle","Hours","Current total");
    private final JTable fleetTable=new JTable(fleetData),rentalTable=new JTable(rentalData);
    private final JComboBox<Vehicle> vehicle=new JComboBox<>();
    private final JComboBox<String> tariff=new JComboBox<>(new String[]{"Hourly","Daily","Weekly / long-term"});
    private final JTextField rentalId=new JTextField(),customer=new JTextField(),promo=new JTextField(),notes=new JTextField();
    private final JSpinner duration=new JSpinner(new SpinnerNumberModel(5,1,87600,1));
    private final JLabel durationLabel=label("Duration (hours)",12,true);
    private final JCheckBox insurance=new JCheckBox("Insurance · $15 / started day"),gps=new JCheckBox("GPS · $10"),seat=new JCheckBox("Child seat · $12");
    private final JLabel total=new JLabel("—"),quoteInfo=new JLabel("Complete the form, then calculate a quote.");
    private final JTextArea receipt=new JTextArea(),events=new JTextArea();
    private boolean refreshing;
    private final java.util.Map<String,JButton> navigation=new java.util.LinkedHashMap<>();

    public MainWindow(RentalDesktopModel model) {
        super(new BorderLayout());this.model=model;
        setMinimumSize(new Dimension(1000,720));setPreferredSize(new Dimension(1280,820));
        JPanel root=new JPanel(new BorderLayout());root.setBackground(BG);add(root,BorderLayout.CENTER);
        root.add(sidebar(),BorderLayout.WEST);
        JPanel workspace=new JPanel(new BorderLayout(0,18));workspace.setOpaque(false);workspace.setBorder(new EmptyBorder(28,28,16,28));root.add(workspace);
        JPanel header=new JPanel(new BorderLayout());header.setOpaque(false);
        heading.setFont(new Font("SansSerif",Font.BOLD,28));heading.setForeground(INK);
        subtitle.setForeground(MUTED);JPanel titles=new JPanel(new GridLayout(2,1,0,4));titles.setOpaque(false);titles.add(heading);titles.add(subtitle);header.add(titles);
        header.add(button("+ New rental",()->navigate("booking"),true),BorderLayout.EAST);workspace.add(header,BorderLayout.NORTH);
        content.setOpaque(false);content.add(overview(),"fleet");content.add(booking(),"booking");content.add(activeRentals(),"rentals");content.add(activity(),"activity");workspace.add(content);
        JPanel footer=new JPanel(new BorderLayout());footer.setOpaque(false);status.setForeground(TEAL);footer.add(status);
        JLabel memory=new JLabel("Session workspace · data resets on close");memory.setForeground(MUTED);footer.add(memory,BorderLayout.EAST);workspace.add(footer,BorderLayout.SOUTH);
        model.onChange(()->SwingUtilities.invokeLater(this::refresh));
        rentalId.setText(model.nextRentalId());watchForm();refresh();navigate("fleet");
    }

    private JPanel sidebar() {
        JPanel p=new JPanel(new BorderLayout());p.setPreferredSize(new Dimension(215,1));p.setBackground(NAVY);p.setBorder(new EmptyBorder(30,18,24,18));
        JLabel brand=new JLabel("RENTAL / DESK");brand.setFont(new Font("SansSerif",Font.BOLD,19));brand.setForeground(Color.WHITE);
        JLabel desc=new JLabel("Fleet & reservations");desc.setForeground(new Color(155,178,195));p.add(vertical(brand,desc),BorderLayout.NORTH);
        JPanel nav=new JPanel();nav.setLayout(new BoxLayout(nav,BoxLayout.Y_AXIS));nav.setOpaque(false);nav.setBorder(new EmptyBorder(40,0,0,0));
        String[][] items={{"fleet","01   Fleet overview"},{"booking","02   Create rental"},{"rentals","03   Active rentals"},{"activity","04   Activity & alerts"}};
        for(String[] item:items){JButton b=button(item[1],()->navigate(item[0]),false);b.setHorizontalAlignment(SwingConstants.LEFT);b.setMaximumSize(new Dimension(190,48));b.setAlignmentX(0);b.setForeground(Color.WHITE);b.setBackground(NAVY);nav.add(b);nav.add(Box.createVerticalStrut(10));navigation.put(item[0],b);}
        p.add(nav);JLabel foot=new JLabel("Java desktop · Swing");foot.setForeground(new Color(155,178,195));p.add(foot,BorderLayout.SOUTH);return p;
    }

    private void navigate(String key) {
        String[][] text={{"fleet","Fleet overview","Your vehicles, availability and hourly rates."},{"booking","Create a rental","Choose a vehicle, tailor the booking and preview the price."},{"rentals","Active rentals","Review reservations and return vehicles to the fleet."},{"activity","Activity & alerts","Live rental events and availability notifications."}};
        for(String[] t:text)if(t[0].equals(key)){heading.setText(t[1]);subtitle.setText(t[2]);}
        for(var n:navigation.entrySet())n.getValue().setBackground(n.getKey().equals(key)?TEAL:NAVY);
        pages.show(content,key);
    }

    private JPanel overview() {
        JPanel page=new JPanel(new BorderLayout(0,18));page.setOpaque(false);
        JPanel cards=new JPanel(new GridLayout(1,4,14,0));cards.setOpaque(false);
        String[] labels={"TOTAL VEHICLES","AVAILABLE NOW","ACTIVE RENTALS","CURRENT RENTAL VALUE"};
        for(int i=0;i<4;i++){metrics[i].setFont(new Font("SansSerif",Font.BOLD,27));metrics[i].setForeground(i==1?TEAL:INK);JLabel l=new JLabel(labels[i]);l.setFont(new Font("SansSerif",Font.BOLD,10));l.setForeground(MUTED);cards.add(card(vertical(l,metrics[i])));}
        page.add(cards,BorderLayout.NORTH);
        JPanel body=new JPanel(new BorderLayout(0,12));body.setOpaque(false);
        JTextField search=new JTextField();search.setToolTipText("Search ID, model or type");name(search,"Search fleet");
        JComboBox<String> filter=new JComboBox<>(new String[]{"All vehicles","Available","Rented"});
        JPanel tools=new JPanel(new BorderLayout(12,0));tools.setOpaque(false);tools.add(field("Search fleet",search));tools.add(field("Availability",filter),BorderLayout.EAST);
        body.add(tools,BorderLayout.NORTH);styleTable(fleetTable);body.add(tableScroll(fleetTable));
        TableRowSorter<DefaultTableModel> sorter=new TableRowSorter<>(fleetData);fleetTable.setRowSorter(sorter);
        Runnable apply=()->{String q=search.getText().trim();java.util.List<RowFilter<Object,Object>> fs=new ArrayList<>();
            if(!q.isEmpty())fs.add(RowFilter.regexFilter("(?i)"+Pattern.quote(q),0,1,2));
            if(filter.getSelectedIndex()>0)fs.add(RowFilter.regexFilter("^"+(filter.getSelectedIndex()==1?"Available":"Rented")+"$",4));
            sorter.setRowFilter(fs.isEmpty()?null:RowFilter.andFilter(fs));};
        onText(search,apply);filter.addActionListener(e->apply.run());
        JPanel actions=new JPanel(new FlowLayout(FlowLayout.LEFT,8,0));actions.setOpaque(false);
        actions.add(button("Register vehicle",this::registerDialog,true));actions.add(button("Rent selected",()->{Vehicle v=selectedFleet();if(v==null)return;if(!v.isAvailable()){error("This vehicle is currently rented.");return;}vehicle.setSelectedItem(v);navigate("booking");},false));
        actions.add(button("Reduce hourly rate",this::rateDialog,false));body.add(actions,BorderLayout.SOUTH);
        page.add(card(body));return page;
    }

    private JPanel booking() {
        JPanel page=new JPanel(new BorderLayout(18,0));page.setOpaque(false);
        JPanel form=new JPanel();form.setLayout(new BoxLayout(form,BoxLayout.Y_AXIS));form.setOpaque(false);
        vehicle.setRenderer(new DefaultListCellRenderer(){public Component getListCellRendererComponent(JList<?> l,Object v,int i,boolean sel,boolean focus){super.getListCellRendererComponent(l,v,i,sel,focus);if(v instanceof Vehicle x)setText(x.getId()+" · "+x.getModel()+" · "+money(x.getBaseHourlyRate())+"/h");return this;}});
        form.add(label("BOOKING DETAILS",11,true));form.add(Box.createVerticalStrut(15));
        form.add(pair(field("Rental ID",rentalId),field("Customer name",customer)));
        form.add(field("Available vehicle",vehicle));
        form.add(pair(field("Pricing policy",tariff),field(durationLabel,duration)));
        form.add(pair(field("Promo code (optional)",promo),field("Pickup notes (optional)",notes)));
        form.add(Box.createVerticalStrut(14));form.add(label("OPTIONAL EXTRAS",11,true));form.add(Box.createVerticalStrut(8));
        for(JCheckBox c:new JCheckBox[]{insurance,gps,seat}){c.setOpaque(false);c.setAlignmentX(0);form.add(c);}
        form.add(Box.createVerticalStrut(20));JLabel policy=label("Daily: 15% off from 1 day (24h). Weekly / long-term: 30% off from 1 week (168h).",11,false);form.add(policy);
        JScrollPane scroll=new JScrollPane(card(form));scroll.setBorder(null);scroll.getVerticalScrollBar().setUnitIncrement(18);page.add(scroll);
        JPanel quote=new JPanel(new BorderLayout(0,15));quote.setOpaque(false);quote.setPreferredSize(new Dimension(300,1));
        total.setFont(new Font("SansSerif",Font.BOLD,40));total.setForeground(TEAL);quoteInfo.setForeground(MUTED);quoteInfo.setFont(new Font("SansSerif",Font.PLAIN,11));
        JPanel summary=vertical(label("PRICE PREVIEW",11,true),total,quoteInfo);summary.setBorder(new EmptyBorder(5,0,5,0));quote.add(summary,BorderLayout.NORTH);
        receipt.setEditable(false);receipt.setLineWrap(true);receipt.setWrapStyleWord(true);receipt.setBackground(Color.WHITE);receipt.setForeground(INK);receipt.setFont(new Font("SansSerif",Font.PLAIN,13));receipt.setText("Your rental summary will appear here.\n\nPrices are in USD. Promo discounts apply to the base rental before extras.\n\nNo payment is collected.");
        quote.add(new JScrollPane(receipt));JPanel buttons=new JPanel(new GridLayout(0,1,0,10));buttons.setOpaque(false);
        buttons.add(button("Calculate quote",this::preview,false));buttons.add(button("Confirm rental",this::book,true));quote.add(buttons,BorderLayout.SOUTH);
        page.add(card(quote),BorderLayout.EAST);return page;
    }

    private JPanel activeRentals() {
        JPanel page=new JPanel(new BorderLayout(0,16));page.setOpaque(false);styleTable(rentalTable);rentalTable.setAutoCreateRowSorter(true);
        page.add(card(tableScroll(rentalTable)));
        JPanel bottom=new JPanel(new BorderLayout());bottom.setOpaque(false);bottom.add(label("Current totals reflect the vehicle's current hourly rate. Select a row to return it.",12,false));
        bottom.add(button("Return selected rental",this::returnSelected,true),BorderLayout.EAST);page.add(bottom,BorderLayout.SOUTH);return page;
    }

    private JPanel activity() {
        JPanel page=new JPanel(new BorderLayout(0,14));page.setOpaque(false);
        page.add(label("Latest events first · availability and price alerts are shown here · last 200 events",12,false),BorderLayout.NORTH);
        events.setEditable(false);events.setLineWrap(true);events.setWrapStyleWord(true);events.setFont(new Font("SansSerif",Font.PLAIN,13));events.setForeground(INK);events.setBackground(Color.WHITE);events.setBorder(new EmptyBorder(16,16,16,16));
        page.add(card(new JScrollPane(events)));return page;
    }

    private void watchForm() {
        for(JTextField f:new JTextField[]{rentalId,customer,promo,notes})onText(f,this::invalidateQuote);
        vehicle.addActionListener(e->invalidateQuote());
        tariff.addActionListener(e->{updateDurationUnit();invalidateQuote();});
        duration.addChangeListener(e->invalidateQuote());
        for(JCheckBox c:new JCheckBox[]{insurance,gps,seat})c.addActionListener(e->invalidateQuote());
    }

    private void updateDurationUnit() {
        String selected=(String)tariff.getSelectedItem();
        if("Daily".equals(selected)){
            durationLabel.setText("Duration (days)");
            name(duration,"Duration (days)");
            duration.setModel(new SpinnerNumberModel(1,1,3650,1));
        }else if("Weekly / long-term".equals(selected)){
            durationLabel.setText("Duration (weeks)");
            name(duration,"Duration (weeks)");
            duration.setModel(new SpinnerNumberModel(1,1,520,1));
        }else{
            durationLabel.setText("Duration (hours)");
            name(duration,"Duration (hours)");
            duration.setModel(new SpinnerNumberModel(5,1,87600,1));
        }
    }

    private int durationInHours() {
        String selected=(String)tariff.getSelectedItem();
        try{duration.commitEdit();}catch(java.text.ParseException ex){
            String unit="Daily".equals(selected)?"days":"Weekly / long-term".equals(selected)?"weeks":"hours";
            throw new IllegalArgumentException("Enter a whole number of "+unit+".");
        }
        int amount=(Integer)duration.getValue();
        if("Daily".equals(selected))return amount*24;
        if("Weekly / long-term".equals(selected))return amount*168;
        return amount;
    }

    private void invalidateQuote(){if(refreshing)return;total.setText("—");quoteInfo.setText("Recalculate after making changes.");receipt.setText("Booking details changed. Calculate a new quote to see the current total.");}

    private RentalDesktopModel.Request request() {
        int totalHours=durationInHours();
        java.util.List<ExtraType> extras=new ArrayList<>();if(insurance.isSelected())extras.add(ExtraType.INSURANCE);if(gps.isSelected())extras.add(ExtraType.GPS);if(seat.isSelected())extras.add(ExtraType.CHILD_SEAT);
        return new RentalDesktopModel.Request(rentalId.getText(),customer.getText(),(Vehicle)vehicle.getSelectedItem(),totalHours,(String)tariff.getSelectedItem(),extras,promo.getText(),notes.getText());
    }

    private void preview(){try{Rental r=model.quote(request());total.setText(money(r.calculateTotalPrice()));quoteInfo.setText("Estimate ("+r.getDurationHours()+"h total) · no vehicle reserved yet");receipt.setText(r.getDescription().replace(" | ","\n\n"));}catch(IllegalArgumentException|IllegalStateException ex){error(ex.getMessage());}}

    private void book(){try{Rental r=model.book(request());status.setText("Rental "+r.getRentalId()+" created · "+money(r.calculateTotalPrice()));rentalId.setText(model.nextRentalId());customer.setText("");promo.setText("");notes.setText("");insurance.setSelected(false);gps.setSelected(false);seat.setSelected(false);updateDurationUnit();refresh();navigate("rentals");}catch(IllegalArgumentException|IllegalStateException ex){error(ex.getMessage());}}

    private Vehicle selectedFleet(){int i=fleetTable.getSelectedRow();if(i<0){error("Select a vehicle in the fleet table first.");return null;}String id=(String)fleetData.getValueAt(fleetTable.convertRowIndexToModel(i),0);return model.vehicles().stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);}

    private void returnSelected(){int i=rentalTable.getSelectedRow();if(i<0){error("Select an active rental first.");return;}String id=(String)rentalData.getValueAt(rentalTable.convertRowIndexToModel(i),0);
        if(JOptionPane.showConfirmDialog(this,"Return rental "+id+" and make its vehicle available?","Return vehicle",JOptionPane.OK_CANCEL_OPTION)!=JOptionPane.OK_OPTION)return;
        try{model.returnRental(id);refresh();status.setText("Rental "+id+" returned. Vehicle available again.");}catch(IllegalArgumentException ex){error(ex.getMessage());}}

    private void registerDialog(){
        JTextField id=new JTextField(),name=new JTextField(),rate=new JTextField();JComboBox<VehicleType> type=new JComboBox<>(VehicleType.values());
        JPanel form=vertical(field("Vehicle ID",id),field("Model",name),field("Vehicle type",type),field("Hourly rate (USD)",rate));form.setPreferredSize(new Dimension(390,300));
        while(JOptionPane.showConfirmDialog(this,form,"Register vehicle",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION){
            try{model.register((VehicleType)type.getSelectedItem(),id.getText(),name.getText(),number(rate.getText()));refresh();status.setText("Vehicle "+id.getText().trim()+" registered.");return;}catch(IllegalArgumentException ex){error(ex.getMessage());}}
    }

    private void rateDialog(){Vehicle v=selectedFleet();if(v==null)return;JTextField rate=new JTextField(String.format(Locale.US,"%.2f",v.getBaseHourlyRate()));
        JPanel p=vertical(label(v.getId()+" · "+v.getModel(),14,true),field("New hourly rate (USD)",rate),label("Must be lower than "+money(v.getBaseHourlyRate())+". Existing rental totals will recalculate.",11,false));
        while(JOptionPane.showConfirmDialog(this,p,"Reduce hourly rate",JOptionPane.OK_CANCEL_OPTION,JOptionPane.PLAIN_MESSAGE)==JOptionPane.OK_OPTION){try{model.reduceRate(v,number(rate.getText()));refresh();status.setText("Rate updated for "+v.getId()+". Listeners notified.");return;}catch(IllegalArgumentException ex){error(ex.getMessage());}}
    }

    private static double number(String text){try{double d=Double.parseDouble(text.trim());if(!Double.isFinite(d)||d<=0)throw new NumberFormatException();return d;}catch(NumberFormatException e){throw new IllegalArgumentException("Enter a finite positive hourly rate, such as 8.50.");}}

    private void refresh(){
        refreshing=true;Vehicle selected=(Vehicle)vehicle.getSelectedItem();fleetData.setRowCount(0);
        for(Vehicle v:model.vehicles())fleetData.addRow(new Object[]{v.getId(),v.getModel(),v.getType(),money(v.getBaseHourlyRate()),v.isAvailable()?"Available":"Rented"});
        rentalData.setRowCount(0);double value=0;for(Rental r:model.rentals()){double price=r.calculateTotalPrice();value+=price;rentalData.addRow(new Object[]{r.getRentalId(),r.getCustomerName(),r.getVehicle().getModel(),r.getDurationHours(),money(price)});}
        metrics[0].setText(""+model.vehicles().size());metrics[1].setText(""+model.available().size());metrics[2].setText(""+model.rentals().size());metrics[3].setText(money(value));
        vehicle.removeAllItems();for(Vehicle v:model.available())vehicle.addItem(v);if(selected!=null&&model.available().contains(selected))vehicle.setSelectedItem(selected);
        events.setText(String.join("\n\n────────────────────────────────\n\n",model.events()));events.setCaretPosition(0);
        refreshing=false;invalidateQuote();
    }

    private void error(String text){JOptionPane.showMessageDialog(this,text,"Check your details",JOptionPane.WARNING_MESSAGE);}
    private static String money(double d){return String.format(Locale.US,"$%,.2f",d);}
    private static DefaultTableModel tableModel(String... columns){return new DefaultTableModel(columns,0){public boolean isCellEditable(int r,int c){return false;}};}
    private static JScrollPane tableScroll(JTable t){JScrollPane p=new JScrollPane(t);p.setColumnHeaderView(t.getTableHeader());return p;}
    private static void styleTable(JTable t){t.setRowHeight(42);t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);t.setShowGrid(false);t.setIntercellSpacing(new Dimension(0,0));t.setFillsViewportHeight(true);t.setBackground(Color.WHITE);t.setForeground(INK);t.setSelectionBackground(new Color(218,240,236));t.setSelectionForeground(INK);t.getTableHeader().setReorderingAllowed(false);t.getTableHeader().setFont(new Font("SansSerif",Font.BOLD,12));t.getTableHeader().setPreferredSize(new Dimension(1,40));}
    private static JButton button(String text,Runnable action,boolean primary){JButton b=new JButton(text){protected void paintComponent(Graphics graphics){Graphics2D g=(Graphics2D)graphics.create();g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);Color c=getBackground();g.setColor(getModel().isRollover()?c.darker():c);g.fillRoundRect(0,0,getWidth(),getHeight(),10,10);g.dispose();super.paintComponent(graphics);}};b.setUI(new javax.swing.plaf.basic.BasicButtonUI());b.setOpaque(false);b.setContentAreaFilled(false);b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(218,228,234)),new EmptyBorder(11,15,11,15)));b.setFocusPainted(false);b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));b.setBackground(primary?TEAL:Color.WHITE);b.setForeground(primary?Color.WHITE:INK);b.setFont(new Font("SansSerif",Font.BOLD,12));b.setMargin(new Insets(10,15,10,15));b.addActionListener(e->action.run());return b;}
    private static JPanel card(Component c){JPanel p=new JPanel(new BorderLayout());p.setBackground(Color.WHITE);p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(222,230,235)),new EmptyBorder(18,18,18,18)));p.add(c);return p;}
    private static JLabel label(String text,int size,boolean bold){JLabel l=new JLabel(text);l.setFont(new Font("SansSerif",bold?Font.BOLD:Font.PLAIN,size));l.setForeground(bold?INK:MUTED);l.setAlignmentX(0);return l;}
    private static JPanel vertical(Component... cs){JPanel p=new JPanel();p.setOpaque(false);p.setLayout(new BoxLayout(p,BoxLayout.Y_AXIS));for(Component c:cs){if(c instanceof JComponent x)x.setAlignmentX(0);p.add(c);p.add(Box.createVerticalStrut(6));}return p;}
    private static JPanel pair(Component a,Component b){JPanel p=new JPanel(new GridLayout(1,2,12,0));p.setOpaque(false);p.add(a);p.add(b);p.setMaximumSize(new Dimension(Integer.MAX_VALUE,85));p.setAlignmentX(0);return p;}
    private static JPanel field(String title,JComponent input){return field(label(title,12,true),input);}
    private static JPanel field(JLabel titleLabel,JComponent input){name(input,titleLabel.getText());JPanel p=new JPanel(new BorderLayout(0,6));p.setOpaque(false);p.setBorder(new EmptyBorder(0,0,12,0));p.add(titleLabel,BorderLayout.NORTH);input.setPreferredSize(new Dimension(160,38));p.add(input);p.setMaximumSize(new Dimension(Integer.MAX_VALUE,80));p.setAlignmentX(0);return p;}
    private static void name(JComponent c,String n){c.setName(n);c.getAccessibleContext().setAccessibleName(n);}
    private static void onText(JTextField f,Runnable r){f.getDocument().addDocumentListener(new DocumentListener(){public void insertUpdate(DocumentEvent e){r.run();}public void removeUpdate(DocumentEvent e){r.run();}public void changedUpdate(DocumentEvent e){r.run();}});}
}