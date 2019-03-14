/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.GenericHID.Hand;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  //Talons for four stilt climb 
  private WPI_TalonSRX frontLeft = new WPI_TalonSRX(1);   
  private WPI_TalonSRX frontRight = new WPI_TalonSRX(2); 
  private WPI_TalonSRX backLeft = new WPI_TalonSRX(3);  
  private WPI_TalonSRX backRight = new WPI_TalonSRX(4);
  private XboxController controller  = new XboxController(0);

  private Solenoid frontLeftClutch = new Solenoid(1);
  private Solenoid frontRightClutch = new Solenoid(2);
  private Solenoid backLeftClutch = new Solenoid(3);
  private Solenoid backRightClutch = new Solenoid(4);

  private boolean enabledPID = false;

  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser); 
    frontLeft.setName("Front Left");  
    frontRight.setName("Front Right"); 
    backLeft.setName("Back Left");   
    backRight.setName("Back Right"); 

    frontLeft.configSelectedFeedbackSensor(FeedbackDevice.Analog, 0, 10);
    frontLeft.configNeutralDeadband(0.001, 10);
    frontRight.configSelectedFeedbackSensor(FeedbackDevice.Analog, 0, 10);
    frontRight.configNeutralDeadband(0.001, 10);
    backLeft.configSelectedFeedbackSensor(FeedbackDevice.Analog, 0, 10);
    backLeft.configNeutralDeadband(0.001, 10);
    backRight.configSelectedFeedbackSensor(FeedbackDevice.Analog, 0, 10);
    backRight.configNeutralDeadband(0.001, 10);

    LiveWindow.addChild(null, frontLeft);
    LiveWindow.addChild(null, frontRight);
    LiveWindow.addChild(null, backLeft);
    LiveWindow.addChild(null, backRight);

    SmartDashboard.putNumber("FL Scale", 1.0);
    SmartDashboard.putNumber("FR Scale", 1.0);
    SmartDashboard.putNumber("BL Scale", 1.0);
    SmartDashboard.putNumber("BR Scale", 1.0);

    SmartDashboard.putNumber("FL Set", frontLeft.getSelectedSensorPosition());
    SmartDashboard.putNumber("FR Set", frontRight.getSelectedSensorPosition());
    SmartDashboard.putNumber("BL Set", backLeft.getSelectedSensorPosition());
    SmartDashboard.putNumber("BR Set", backRight.getSelectedSensorPosition());

    SmartDashboard.putNumber("Lift Delta", 0.0);

    SmartDashboard.putBoolean("PID Enabled", enabledPID) ;

    smartdashboard_SendorData();
  }

  private void smartdashboard_SendorData(){
    SmartDashboard.putNumber("FL Pos", frontLeft.getSelectedSensorPosition());
    SmartDashboard.putNumber("FR Pos", frontRight.getSelectedSensorPosition());
    SmartDashboard.putNumber("BL Pos", backLeft.getSelectedSensorPosition());
    SmartDashboard.putNumber("BR Pos", backRight.getSelectedSensorPosition());

    SmartDashboard.putNumber("FL Err", frontLeft.getClosedLoopError());
    SmartDashboard.putNumber("FR Err", frontRight.getClosedLoopError());
    SmartDashboard.putNumber("BL Err", backLeft.getClosedLoopError());
    SmartDashboard.putNumber("BR Err", backRight.getClosedLoopError());

    SmartDashboard.putBoolean("FL Clutch", frontLeftClutch.get());
    SmartDashboard.putBoolean("FR Clutch", frontRightClutch.get());
    SmartDashboard.putBoolean("BL Clutch", backLeftClutch.get());
    SmartDashboard.putBoolean("BR Clutch", backRightClutch.get());

    SmartDashboard.putBoolean("PID Enabled", enabledPID) ;
  }

  private void smartdashboard_ClutchControl(){
    frontLeftClutch.set( SmartDashboard.getBoolean("FL Clutch", frontLeftClutch.get()) );
    frontRightClutch.set( SmartDashboard.getBoolean("FR Clutch", frontRightClutch.get()) );
    backLeftClutch.set( SmartDashboard.getBoolean("BL Clutch", backLeftClutch.get()) );
    backRightClutch.set( SmartDashboard.getBoolean("BR Clutch", backRightClutch.get()) );
  }

  private void smartdashboard_LiftDelta(){
    double liftDelta = SmartDashboard.getNumber("Lift Delta", 0);
    if (liftDelta == 0)
      return ;

    SmartDashboard.putNumber("Lift Delta", 0);
    frontLeft.set(ControlMode.Position, frontLeft.getSelectedSensorPosition()+liftDelta);
    frontRight.set(ControlMode.Position, frontRight.getSelectedSensorPosition()+liftDelta);
    backLeft.set(ControlMode.Position, backLeft.getSelectedSensorPosition()+liftDelta);
    backRight.set(ControlMode.Position, backRight.getSelectedSensorPosition()+liftDelta);
  }

  double maxCurrent = 0.0;
  private void smartdashboard_Current(){
    double avgCurrent  = (frontLeft.getOutputCurrent() + frontRight.getOutputCurrent() +
    backLeft.getOutputCurrent() + backRight.getOutputCurrent()) / 4.0;

   if(avgCurrent > maxCurrent){
     maxCurrent = avgCurrent;
   }

   SmartDashboard.putNumber("Max Current", maxCurrent);
   SmartDashboard.putNumber("Average Current", avgCurrent);
  }

  @Override
  public void disabledInit() {
    frontLeft.set(ControlMode.PercentOutput, 0);
    frontRight.set(ControlMode.PercentOutput, 0);
    backLeft.set(ControlMode.PercentOutput, 0);
    backRight.set(ControlMode.PercentOutput, 0);
    }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto:
        // Put custom auto code here
        break;
      case kDefaultAuto:
      default:
        // Put default auto code here
        break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    if (controller.getStartButtonPressed()){
      enabledPID = true;

      double fl_set = SmartDashboard.getNumber("FL Set", frontLeft.getSelectedSensorPosition());
      double fr_set = SmartDashboard.getNumber("FR Set", frontRight.getSelectedSensorPosition());
      double bl_set = SmartDashboard.getNumber("BL Set", backLeft.getSelectedSensorPosition());
      double br_set = SmartDashboard.getNumber("BR Set", backRight.getSelectedSensorPosition());

      frontLeft.set(ControlMode.Position, fl_set);
      frontRight.set(ControlMode.Position, fr_set);
      backLeft.set(ControlMode.Position, bl_set);
      backRight.set(ControlMode.Position, br_set);
    }

    if (controller.getBackButtonPressed()){
      enabledPID = false;
      frontLeft.set(ControlMode.PercentOutput, 0);
      frontRight.set(ControlMode.PercentOutput, 0);
      backLeft.set(ControlMode.PercentOutput, 0);
      backRight.set(ControlMode.PercentOutput, 0);
    }

    if (enabledPID){
      int liftDelta = (int)(controller.getTriggerAxis(Hand.kRight) * 10)
                      - (int)(controller.getTriggerAxis(Hand.kLeft) * 10);
      if (liftDelta > 0) {
        frontLeft.set(ControlMode.Position, frontLeft.getSelectedSensorPosition()+liftDelta);
        frontRight.set(ControlMode.Position, frontRight.getSelectedSensorPosition()+liftDelta);
        backLeft.set(ControlMode.Position, backLeft.getSelectedSensorPosition()+liftDelta);
        backRight.set(ControlMode.Position, backRight.getSelectedSensorPosition()+liftDelta);
      }
    }
    else {
      double speed = controller.getTriggerAxis(Hand.kRight) - controller.getTriggerAxis(Hand.kLeft);

      double speedLimit = 0.75;
      double rightSpeed = 0.0;
      double leftSpeed = 0.0;
      double frontSpeed = 0.0;
      double backSpeed = 0.0;
  
      if(controller.getX(Hand.kRight) > 0.0){
        rightSpeed = Math.abs(controller.getX(Hand.kRight)) * speedLimit;
      }
      if(controller.getX(Hand.kRight) < 0.0){
        leftSpeed = Math.abs(controller.getX(Hand.kRight)) *speedLimit;
      }
      if(controller.getY(Hand.kRight) > 0.0 ){
        backSpeed = Math.abs(controller.getY(Hand.kRight)) * speedLimit;
      }
      if(controller.getY(Hand.kRight) < 0.0){
        frontSpeed = Math.abs(controller.getY(Hand.kRight)) * speedLimit;
      }
    
      double flScale = SmartDashboard.getNumber("FL Scale", 1.0);
      double frScale =  SmartDashboard.getNumber("FR Scale", 1.0);
      double blScale = SmartDashboard.getNumber("BL Scale", 1.0);
      double brScale  = SmartDashboard.getNumber("BR Scale" , 1.0);
  
      frontLeft.set(speed * flScale + frontSpeed + leftSpeed);
      frontRight.set(speed * frScale + frontSpeed + rightSpeed);
      backLeft.set(speed * blScale + backSpeed + leftSpeed);
      backRight.set(speed * brScale + backSpeed + rightSpeed);
     }

    if (controller.getYButtonPressed()){
      frontLeftClutch.set( !frontLeftClutch.get() );
    }
    if (controller.getBButtonPressed()){
      frontRightClutch.set( !frontRightClutch.get() );
    }
    if (controller.getXButtonPressed()){
      backLeftClutch.set( !backLeftClutch.get() );
    }
    if (controller.getAButtonPressed()){
      backRightClutch.set( !backRightClutch.get() );
    }

    smartdashboard_Current();
    smartdashboard_SendorData();
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {   


  }

/*──────────────███████──███████
──────────████▓▓▓▓▓▓████░░░░░██
────────██▓▓▓▓▓▓▓▓▓▓▓▓██░░░░░░██
──────██▓▓▓▓▓▓████████████░░░░██
────██▓▓▓▓▓▓████████████████░██
────██▓▓████░░░░░░░░░░░░██████
──████████░░░░░░██░░██░░██▓▓▓▓██
──██░░████░░░░░░██░░██░░██▓▓▓▓██
██░░░░██████░░░░░░░░░░░░░░██▓▓██
██░░░░░░██░░░░██░░░░░░░░░░██▓▓██
──██░░░░░░░░░███████░░░░██████
────████░░░░░░░███████████▓▓██
──────██████░░░░░░░░░░██▓▓▓▓██
────██▓▓▓▓██████████████▓▓██
──██▓▓▓▓▓▓▓▓████░░░░░░████
████▓▓▓▓▓▓▓▓██░░░░░░░░░░██
████▓▓▓▓▓▓▓▓██░░░░░░░░░░██
██████▓▓▓▓▓▓▓▓██░░░░░░████████
──██████▓▓▓▓▓▓████████████████
────██████████████████████▓▓▓▓██
──██▓▓▓▓████████████████▓▓▓▓▓▓██
████▓▓██████████████████▓▓▓▓▓▓██
██▓▓▓▓██████████████████▓▓▓▓▓▓██
██▓▓▓▓██████████──────██▓▓▓▓████
██▓▓▓▓████──────────────██████ 
──████
*/
}                                                  
